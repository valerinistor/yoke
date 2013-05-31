package com.jetdrone.vertx.yoke.extras.stores;

import com.jetdrone.vertx.yoke.util.YokeAsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class MongoDbStore implements Store {

    final EventBus eb;
    final String address;
    final String collection;

    public MongoDbStore(EventBus eb, String address, String collection) {
        this.eb = eb;
        this.address = address;
        this.collection = collection;
    }

    @Override
    public void read(String id, final AsyncResultHandler<JsonObject> handler) {
        JsonObject wrapper = new JsonObject();
        wrapper.putString("collection", collection);
        wrapper.putString("action", "findone");
        wrapper.putObject("matcher", new JsonObject().putString("_id", id));

        eb.send(address, wrapper, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                String status = reply.body().getString("status");

                if (status != null && "ok".equalsIgnoreCase(status)) {
                    JsonObject result = reply.body().getObject("result");

                    handler.handle(new YokeAsyncResult<>(null, result));
                } else {
                    handler.handle(new YokeAsyncResult<JsonObject>(new Throwable(status), null));
                }
            }
        });
    }

    private static Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    @Override
    public void query(JsonObject query, String start, String end, JsonObject sort, final AsyncResultHandler<JsonArray> handler) {
        JsonObject wrapper = new JsonObject();
        wrapper.putString("collection", collection);
        wrapper.putString("action", "find");
        wrapper.putObject("matcher", query);

        Integer iStart = parseInt(start);
        Integer iEnd = parseInt(end);

        if (iStart != null) {
            wrapper.putNumber("skip", iStart);
            if (iEnd != null) {
                int limit = iEnd - iStart;
                wrapper.putNumber("limit", limit);
            }
        }

        if (sort != null) {
            wrapper.putObject("sort", sort);
        }

        eb.send(address, wrapper, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                String status = reply.body().getString("status");

                if (status != null && "ok".equalsIgnoreCase(status)) {
                    JsonArray result = reply.body().getArray("results");

                    handler.handle(new YokeAsyncResult<>(null, result));
                } else {
                    handler.handle(new YokeAsyncResult<JsonArray>(new Throwable(status), null));
                }
            }
        });
    }

    @Override
    public void count(JsonObject query, final AsyncResultHandler<Number> handler) {
        JsonObject wrapper = new JsonObject();
        wrapper.putString("collection", collection);
        wrapper.putString("action", "count");
        wrapper.putObject("matcher", query);
        eb.send(address, wrapper, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                String status = reply.body().getString("status");

                if (status != null && "ok".equalsIgnoreCase(status)) {
                    Number result = reply.body().getNumber("count");

                    handler.handle(new YokeAsyncResult<>(null, result));
                } else {
                    handler.handle(new YokeAsyncResult<Number>(new Throwable(status), null));
                }
            }
        });
    }

    @Override
    public void delete(String id, final AsyncResultHandler<Number> handler) {
        JsonObject wrapper = new JsonObject();
        wrapper.putString("collection", collection);
        wrapper.putString("action", "delete");
        wrapper.putObject("matcher", new JsonObject().putString("_id", id));

        eb.send(address, wrapper, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                String status = reply.body().getString("status");

                if (status != null && "ok".equalsIgnoreCase(status)) {
                    Number result = reply.body().getNumber("number");

                    handler.handle(new YokeAsyncResult<>(null, result));
                } else {
                    handler.handle(new YokeAsyncResult<Number>(new Throwable(status), null));
                }
            }
        });
    }

    @Override
    public void create(JsonObject document, final AsyncResultHandler<String> handler) {
        JsonObject wrapper = new JsonObject();
        wrapper.putString("collection", collection);
        wrapper.putString("action", "save");
        wrapper.putObject("document", document);

        eb.send(address, wrapper, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                String status = reply.body().getString("status");

                if (status != null && "ok".equalsIgnoreCase(status)) {
                    String result = reply.body().getString("_id");
                    handler.handle(new YokeAsyncResult<>(null, result));
                } else {
                    handler.handle(new YokeAsyncResult<String>(new Throwable(status), null));
                }
            }
        });
    }

    @Override
    public void update(String id, JsonObject newDocument, final AsyncResultHandler<Number> handler) {
        JsonObject wrapper = new JsonObject();
        wrapper.putString("collection", collection);
        wrapper.putString("action", "update");
        wrapper.putObject("criteria", new JsonObject().putString("_id", id));

        wrapper.putObject("objNew", newDocument);
        eb.send(address, wrapper, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                String status = reply.body().getString("status");

                if (status != null && "ok".equalsIgnoreCase(status)) {
                    Number result = reply.body().getNumber("number");

                    handler.handle(new YokeAsyncResult<>(null, result));
                } else {
                    handler.handle(new YokeAsyncResult<Number>(new Throwable(status), null));
                }
            }
        });
    }
}