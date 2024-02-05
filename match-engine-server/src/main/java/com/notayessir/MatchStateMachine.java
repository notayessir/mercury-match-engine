package com.notayessir;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.notayessir.bo.*;
import com.notayessir.constant.EnumMatchCommand;
import com.notayessir.constant.EnumMatchResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.ratis.io.MD5Hash;
import org.apache.ratis.proto.RaftProtos;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftGroupMemberId;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.protocol.TermIndex;
import org.apache.ratis.server.storage.FileInfo;
import org.apache.ratis.server.storage.RaftStorage;
import org.apache.ratis.statemachine.StateMachineStorage;
import org.apache.ratis.statemachine.TransactionContext;
import org.apache.ratis.statemachine.impl.BaseStateMachine;
import org.apache.ratis.statemachine.impl.SimpleStateMachineStorage;
import org.apache.ratis.statemachine.impl.SingleFileSnapshotInfo;
import org.apache.ratis.util.MD5FileUtil;
import org.roaringbitmap.longlong.LongBitmapDataProvider;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Slf4j
public class MatchStateMachine extends BaseStateMachine {



    private final SimpleStateMachineStorage storage = new SimpleStateMachineStorage();
    private final Map<Long, OrderBookBO> orderBooks = new HashMap<>();

    private LongBitmapDataProvider bitmap = new Roaring64NavigableMap();

    private Long globalSequence = 0L;

    private final Publisher publisher;

    public MatchStateMachine(Publisher publisher) {
        this.publisher = publisher;
    }


    public record MatchState(Map<Long, OrderBookBO> orderBooks, LongBitmapDataProvider bitmap, Long globalSequence) {}


    private synchronized MatchState getState(){
        return new MatchState(orderBooks, bitmap, globalSequence);
    }

    private synchronized void updateState(Map<Long, OrderBookBO> orderBooks, LongBitmapDataProvider bitmap, Long globalSequence) {
        this.orderBooks.clear();
        this.orderBooks.putAll(orderBooks);

        this.bitmap = bitmap;
        this.globalSequence = globalSequence;
    }



    @Override
    public void initialize(RaftServer raftServer, RaftGroupId raftGroupId, RaftStorage raftStorage) throws IOException {
        super.initialize(raftServer, raftGroupId, raftStorage);

        storage.init(raftStorage);
        loadSnapshot(storage.getLatestSnapshot());
    }


    @Override
    public StateMachineStorage getStateMachineStorage() {
        return storage;
    }

    @Override
    public void reinitialize() throws IOException {
        setLastAppliedTermIndex(null);
        loadSnapshot(storage.getLatestSnapshot());
    }

    private void loadSnapshot(SingleFileSnapshotInfo snapshot) throws IOException {
        if (Objects.isNull(snapshot)) {
            return ;
        }
        final Path snapshotPath = snapshot.getFile().getPath();
        if (!Files.exists(snapshotPath)) {
            log.warn("The snapshot file {} does not exist for snapshot {}", snapshotPath, snapshot);
            return ;
        }
        MD5Hash md5 = snapshot.getFile().getFileDigest();
        if (Objects.nonNull(md5)) {
            MD5FileUtil.verifySavedMD5(snapshotPath.toFile(), md5);
        }

        TermIndex last = SimpleStateMachineStorage.getTermIndexFromSnapshotFile(snapshotPath.toFile());

        Map<Long, OrderBookBO> orderBooks = new HashMap<>();
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(snapshotPath)))) {
            setLastAppliedTermIndex(last);

            long sequence = in.readLong();
            int len = in.readInt();
            byte[] bytes = in.readNBytes(len);
            LongBitmapDataProvider bitmap = JSONB.parseObject(bytes, Roaring64NavigableMap.class);

            int bookLen = in.readInt();
            for (int i = 0; i < bookLen; i++){
                Long productId = in.readLong();
                long seq = in.readLong();

                OrderBookBO orderBookBO = new OrderBookBO(seq);
                Map<Long, OrderItemBO> orders = orderBookBO.getOrders();
                // ask
                OrderTreemapBO ask = orderBookBO.getAsk();
                int queueLen = in.readInt();
                for (int j = 0; j < queueLen; j++){
                    len = in.readInt();
                    bytes = in.readNBytes(len);
                    OrderItemBO matchOrderBO = JSONB.parseObject(bytes, OrderItemBO.class);

                    ask.addOrder(matchOrderBO);
                    orders.put(matchOrderBO.getOrderId(), matchOrderBO);
                }

                // bid
                OrderTreemapBO bid = orderBookBO.getBid();
                queueLen = in.readInt();
                for (int j = 0; j < queueLen; j++){
                    len = in.readInt();
                    bytes = in.readNBytes(len);
                    OrderItemBO matchOrderBO = JSONB.parseObject(bytes, OrderItemBO.class);

                    bid.addOrder(matchOrderBO);
                    orders.put(matchOrderBO.getOrderId(), matchOrderBO);
                }

                orderBooks.put(productId, orderBookBO);
            }

            //update state
            updateState(orderBooks, bitmap, sequence);
        }
    }



    @Override
    public long takeSnapshot() {
        log.info("start taking snapshot, date:{}", new Date());

        MatchState state = getState();
        TermIndex last = getLastAppliedTermIndex();

        //create a file with a proper name to store the snapshot
        File snapshotFile = storage.getSnapshotFile(last.getTerm(), last.getIndex());

        //write the counter value into the snapshot file
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(
                Files.newOutputStream(snapshotFile.toPath())))) {

            out.writeLong(state.globalSequence());

            LongBitmapDataProvider bitmap = state.bitmap();
            byte[] b2 = JSONB.toBytes(bitmap);
            out.writeInt(b2.length);
            out.write(b2);


            Map<Long, OrderBookBO> orderBooks = state.orderBooks();

            out.writeInt(orderBooks.size());

            Set<Map.Entry<Long, OrderBookBO>> entries = orderBooks.entrySet();
            for (Map.Entry<Long, OrderBookBO> entry : entries) {
                Long productId = entry.getKey();
                out.writeLong(productId);
                OrderBookBO value = entry.getValue();
                long seq = value.getTxSequence();
                out.writeLong(seq);

                OrderTreemapBO ask = value.getAsk();
                Set<Map.Entry<BigDecimal, OrderQueueBO>> askEntrySet = ask.entrySet();
                out.writeInt(askEntrySet.size());
                for (Map.Entry<BigDecimal, OrderQueueBO> askEntry : askEntrySet) {
                    BigDecimal price = askEntry.getKey();
                    byte[] bytes = JSONB.toBytes(price);
                    out.writeInt(bytes.length);
                    out.write(bytes);

                    OrderQueueBO orderQueueBO = askEntry.getValue();
                    out.writeInt(orderQueueBO.size());
                    Set<Map.Entry<Long, OrderItemBO>> orderEntrySet = orderQueueBO.entrySet();
                    for (Map.Entry<Long, OrderItemBO> orderBOEntry : orderEntrySet) {
                        Long orderId = orderBOEntry.getKey();
                        OrderItemBO matchOrderBO = orderBOEntry.getValue();
                        bytes = JSONB.toBytes(matchOrderBO);
                        out.writeInt(bytes.length);
                        out.write(bytes);
                    }

                }

                OrderTreemapBO bid = value.getBid();
                Set<Map.Entry<BigDecimal, OrderQueueBO>> bidEntrySet = bid.entrySet();
                out.writeInt(bidEntrySet.size());
                for (Map.Entry<BigDecimal, OrderQueueBO> bidEntry : bidEntrySet) {
                    BigDecimal price = bidEntry.getKey();
                    byte[] bytes = JSONB.toBytes(price);
                    out.writeInt(bytes.length);
                    out.write(bytes);

                    OrderQueueBO orderQueueBO = bidEntry.getValue();
                    out.writeInt(orderQueueBO.size());
                    Set<Map.Entry<Long, OrderItemBO>> orderEntrySet = orderQueueBO.entrySet();
                    for (Map.Entry<Long, OrderItemBO> orderBOEntry : orderEntrySet) {
                        Long orderId = orderBOEntry.getKey();
                        OrderItemBO matchOrderBO = orderBOEntry.getValue();
                        bytes = JSONB.toBytes(matchOrderBO);
                        out.writeInt(bytes.length);
                        out.write(bytes);
                    }
                }
            }

        } catch (IOException ioe) {
            log.warn("Failed to write snapshot file \"" + snapshotFile
                    + "\", last applied index=" + last, ioe);
        }

        // update storage
        MD5Hash md5 = MD5FileUtil.computeAndSaveMd5ForFile(snapshotFile);
        FileInfo info = new FileInfo(snapshotFile.toPath(), md5);
        storage.updateLatestSnapshot(new SingleFileSnapshotInfo(info, last));

        log.info("finish taking snapshot, date:{}", new Date());
        //return the index of the stored snapshot (which is the last applied one)
        return last.getIndex();
    }

    @Override
    public void notifyLeaderChanged(RaftGroupMemberId groupMemberId, RaftPeerId newLeaderId) {
        log.info("leadership, newLeaderId {}", newLeaderId);
    }


    @Override
    public CompletableFuture<Message> applyTransaction(TransactionContext trx) {
        RaftProtos.LogEntryProto entry = trx.getLogEntry();
        TermIndex termIndex = TermIndex.valueOf(entry);
        String logData = entry.getStateMachineLogEntry().getLogData().toString(Charset.defaultCharset());
        MatchCommandBO command = JSONObject.parseObject(logData, MatchCommandBO.class);

        // check idempotent
        boolean contained = bitmap.contains(command.getRequestId());
        if (contained){
            return CompletableFuture.completedFuture(Message.valueOf(String.valueOf(EnumMatchResp.IDEMPOTENT.getCode())));
        }
        bitmap.addLong(command.getRequestId());

        // do match
        MatchResultBO resultBO = match(command);

        // must update index once state machine is updated
        updateLastAppliedTermIndex(termIndex);

        if (trx.getServerRole() == RaftProtos.RaftPeerRole.LEADER){
            publisher.publish(resultBO);
        }
        return CompletableFuture
                .completedFuture(Message.valueOf(String.valueOf(EnumMatchResp.SUCCESS.getCode())));
    }


    private MatchResultBO match(MatchCommandBO command) {
        OrderBookBO orderBookBO = orderBooks.get(command.getCoinId());
        if (Objects.isNull(orderBookBO)){
            orderBookBO = new OrderBookBO(0L);
            orderBooks.put(command.getCoinId(), orderBookBO);
        }

        MatchResultBO resultBO;
        if (command.getCommand() == EnumMatchCommand.CANCEL.getCode()){
            // CANCEL
            resultBO = orderBookBO.cancel(command.getOrderId());
        } else {
            // PLACE
//            OrderItemBO matchOrderBO = buildPlaceOrder(command);
            resultBO = orderBookBO.place(command);
        }
        resultBO.setGlobalSequence(++globalSequence);
        resultBO.setCommandType(command.getCommand());
        resultBO.setTimestamp(System.currentTimeMillis());
        return resultBO;

    }






}
