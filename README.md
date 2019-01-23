# Stanford Core NLP Analyzer for Elasticsearch

This project is used for build a plugin for elasticsearch to involve the Stanford NLP analyzer.

## Stanford NLP?

The stanford NLP project is an open-source project (which is licensed under the GNU General Public License V3 or later)\
which used for providing a set of human language technology tools\
This project as its name said, was maintaining by the Standford group.\
ref:\
[Stanford CoreNLP home page](https://stanfordnlp.github.io/CoreNLP/index.html)\
[Stanford CoreNLP GitHub page](https://github.com/stanfordnlp/CoreNLP)

## Why stanford core NLP?

Yes, we do have several open-source analyzers for ES, include the words popular IK, Jieba and
some other NLP analyzers provided by some group and companies.
And yes, each of them has their advantages.

But having compared a branch of test cases among all these analyzers of both open-source ones and commercialized,
we found that it seems the Stanford NLP is the most fitful for our project as we need not only sentences' separate,
but also the sentiments' analyze.

## Why this project?

I searched both Google and Github and asked for help on the professional forum
([elasticsearch China](https://elasticsearch.cn/)),
and found that It seems not to have a stable project which could provide this requirement.

Well, there is only one choice for me, build a fitful plugin to make it done. :)

## Use?

### install by git
1. I suppose your machine has prepared the Java JDK 8, Maven, Git .etc
2. git clone this project
3. maven package this project `mvn clean install -e -U`
4. copy the jar packages into plugin folder:
    1. the project jar of course
    1. stanford-corenlp-3.9.2.jar
    1. stanford-chinese-corenlp-models-current.jar
    1. commons-logging-1.2.jar
4. restart the ES and enjoy

### install by archive file
1. download the release file
2. untar into ES plugin folder
3. restart the ES and enjoy
 
## Quick Example
1. Create an index
```bash
curl -XPUT http://localhost:9200/index  -H 'Content-Type:application/json' -d'
{
  "settings": {
    "number_of_replicas": 0,
    "number_of_shards": 1
  },
  "mappings": {
    "_doc": {
      "properties": {
        "id": {
          "type": "integer"
        },
        "text": {
          "type": "text",
          "analyzer": "stanford-core-nlp",
          "search_analyzer": "stanford-core-nlp"
        }
      }
    }
  }
}
'
```

2. Index some docs
```bash
curl -XPOST http://localhost:9200/index/_doc/1 -H 'Content-Type:application/json' -d'
{"id":1, "text":"中美贸易摩擦到贸易战"}
'
```

```bash
curl -XPOST http://localhost:9200/index/_doc/2 -H 'Content-Type:application/json' -d'
{"id":2, "text":"美国和墨西哥重新签订美墨贸易协定"}
'
```

```bash
curl -XPOST http://localhost:9200/index/_doc/3 -H 'Content-Type:application/json' -d'
{"id":3, "text":"知乎裁员意味着互联网寒冬的到来"}
'
```

```bash
curl -XPOST http://localhost:9200/index/_doc/4 -H 'Content-Type:application/json' -d'
{"id":4, "text":"比亚迪公开被撕拖欠款项，声称欠款方伪造公章"}
'
```

3.query with highlighting

```bash
curl -XPOST http://localhost:9200/index/_search  -H 'Content-Type:application/json' -d'
{
    "query" : { "match" : { "text" : "美国" }},
    "highlight" : {
        "pre_tags" : ["<tag1>", "<tag2>"],
        "post_tags" : ["</tag1>", "</tag2>"],
        "fields" : {
            "text" : {}
        }
    }
}
'
```

Result
```json
{
  "took": 192,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": 2,
    "max_score": 0.92510056,
    "hits": [
      {
        "_index": "index",
        "_type": "_doc",
        "_id": "1",
        "_score": 0.92510056,
        "_source": {
          "id": 1,
          "text": "美国政府瘫痪"
        },
        "highlight": {
          "text": [
            "<tag1>美国</tag1>政府瘫痪"
          ]
        }
      },
      {
        "_index": "index",
        "_type": "_doc",
        "_id": "2",
        "_score": 0.65024257,
        "_source": {
          "id": 2,
          "text": "美国和墨西哥重新签订美墨贸易协定"
        },
        "highlight": {
          "text": [
            "<tag1>美国</tag1>和墨西哥重新签订美墨贸易协定"
          ]
        }
      }
    ]
  }
}
```

## Shit happens?
1. xx access denied (e.g. java.lang.RuntimePermission xxxx)?\
-> Add these into plugin-security.policy
    *   permission java.lang.RuntimePermission "*";
    *   permission java.lang.reflect.ReflectPermission "*";
-> Edit the `jvm.options `
    *   add the following cmd
        -Djava.security.policy=file://${dir of this}/plugin-security.policy

2. xx gc xx overhead?\
-> Edit the `jvm.options`
    *   change the Xms && Xmx to be a larger size
    

