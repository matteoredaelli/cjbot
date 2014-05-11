cjbot
=====

[cjbot](https://github.com/matteoredaelli/cjbot) is crawler written in Clojure

Contributors:

* [Matteo Redaelli](http://www.redaelli.org/matteo/)

Requirement
-----------

* Java JDK 1.6 or 1.7

* lein

* MongoDB

* Redis

Supported sources:
* twitter

Usage
------------


Implementation
----------------

Queues:

* cjbot.crawler.req

{\"id\":\"zzzz\", \"source\":\"twitter\", \"body\": {\"key\":\"lists-members\", \"value\": {\"list-id\":21931460}}}

PUBLISH cjbot.crawler.req '{"id":"zzzz", "source":"twitter", "body": {"key":"lists-members", "value": {"list-id":"21931460"}}}'

PUBLISH cjbot.crawler.req '{"id":"zzzz", "source":"twitter", "body": {"key":"users-suggestions", "value": {"lang":"en"}}}'

PUBLISH cjbot.crawler.req '{"id":"zzzz", "source":"twitter", "body": {"key":"followers-ids", "value": {"user-id":"461306712"}}}'

* cjbot.crawler.resp

* cjbot.db.update.req
  {\"id\":\"zzzz\", \"source\":\"twitter\", \"body\": {\"key\":\"users\", \"value\": {json status}}}


Mongo

{
	_id:
	object: "user"
	source: "twitter"
	lookup: { .. }
	friends: [id1,id2],
	followers: [id1, id2]
	timeline: {..]
}
