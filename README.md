# Readme

## Setup

Install zed via homebrew

```
brew install authzed/tap/spicedb authzed/tap/zed
```

Run the docker image:

```
docker run --rm -p 50051:50051 -d authzed/spicedb serve --grpc-preshared-key "somerandomkeyhere"
```

Set up the client:

```
zed context set dev localhost:50051 somerandomkeyhere --insecure
```

Check if the connection is working:

```
zed version
client: zed v0.11.1
service: v1.23.1
```

If you get a service version it's working!

Create the schema:

```
zed schema write schema.zed
```

And add example data:

```
zed relationship touch book:1 viewer user:bob
zed relationship touch book:1 viewer user:alice
zed relationship touch book:2 viewer user:bob
zed relationship touch book:2 viewer user:alice
zed relationship touch book:3 viewer user:bob
zed relationship touch book:4 viewer user:alice
zed relationship touch book:5 viewer user:bob
zed relationship touch book:6 viewer user:alice
zed relationship touch book:7 viewer user:bob
zed relationship touch book:8 viewer user:alice
zed relationship touch book:9 viewer user:alice
zed relationship touch book:10 viewer user:alice
zed relationship touch book:11 viewer user:alice
zed relationship touch book:12 viewer user:alice
```

And validate the setup:

```
zed permission check book:1 view user:alice
zed permission check book:1 view user:bob
zed permission check book:12 view user:bob
 zed permission check book:12 view user:alice
```

If you changed the "somerandomkeyhere" token, be sure to update it in SpiceDB.kt

## Resources

- https://medium.com/kpmg-uk-engineering/getting-started-with-spicedb-in-net-741e353a4d83
- https://github.com/authzed/authzed-java
- https://play.authzed.com/schema
