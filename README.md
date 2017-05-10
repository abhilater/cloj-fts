# cloj-fts

Proof of concept full text search service built entire with Clojure

## Introduction
A Proof of concept full text search service over simple REST endpoints built on Clojure.

HTTP API. There are 2 endpoints

1. POST /document
```
{
"id": "123",
"text": "We are going to build a HUUUUUGEEE wall"
}
```

Clients post a unique identified with each document. “text” field contains all the text to be indexed.

2. GET /search?query="WALL"
```
{
"count":1,
"documents":[
    {
    "id": "123",
    "text": "We are going to build a HUUUUUGEEE wall"
    }
   ]
}
```

Search api returns all documents that match the query text. The secret to search engine’s low latency is
it's in-memory index. The search engine "tokenises" and "normalises" all search input.

A tokenizer is a program that takes a input and emits one or more tokens. Default tokeniser would
spilt on spaces. For example for input "We are going to build a HUUUUUGEEE wall", tokeniser
would generate 8 tokens, namely: "We", "are", "going", "to", "build", "a", "HUUUUUGEEE", "wall".

A normaliser converts the input to a normalised form. Default normaliser would convert all strings
to lowercase. For example for token "HUUUUUGEEE" will be normalised to "huuuuugeee".

A good implementation would allows users to configure and chain multiple tokenisers and
normalisers according to their use case. Additionally, it would be great if users could provide their
own implementations too.

## Installation

1. Download/Clone from https://github.com/abhilater/cloj-fts
2. Run server with leiningen


```
lein ring server

```
3. Service starts at http://localhost:3000/documents

```
{"200":"Success!"}

```

## Usage

: explanation

1. Create documents

```
curl -X POST --data "text=We are going to create a HUGE Wall" http://localhost:3000/documents

{"created":{"id":1,"text":"We are going to create a HUGE Wall"}}

```

```
curl -X POST --data "text=YES we are" http://localhost:3000/documents

{"created":{"id":2,"text":"YES we are"}}

```

2. Search documents

```
http://localhost:3000/documents/search?query=YES+are

{"count":2,"documents":[{"id":2,"text":"YES we are"},{"id":1,"text":"We are going to create a HUGE Wall"}]}

```

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
