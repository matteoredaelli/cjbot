(ns cjbot.redis
  (:gen-class)
  (:use [carica.core])
  (:require 
   [clojure.data.json :as json]
   [taoensso.carmine :as car :refer (wcar)]
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]))

(def redis-conn {:pool {} :spec {:host (config :redis :host)
                                 :port (config :redis :port)}})

(defmacro wcar* [& body] `(car/wcar redis-conn ~@body))

(defn encode-message [msg]
  (json/write-str msg))

(defn decode-message [msg]
  (json/read-json msg))

(defn save-key-hash-value [key hash value]
  (warn "Saving REDIS key =" key)
  (wcar* (car/hsetnx key hash (encode-message value))))

(defn save-key-value-hash [category object id hash value]
  (let [key (format "%s:%s:%d" category object id)]
    (save-key-hash-value key hash value)
    (warn "Saving to REDIS key =" key "hash =" hash)))

(defn cjbot-queue-name-with-prefix [queue]
  (format "%s.%s" (config :redis :prefix) queue))

(defn cjbot-mq-publish [queue msg]
  (let [msg-json (encode-message msg)
        queue (cjbot-queue-name-with-prefix queue)]
    (wcar* (car/publish queue msg-json))))

