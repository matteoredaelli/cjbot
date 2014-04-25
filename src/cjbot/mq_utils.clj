(ns cjbot.mq_utils
  (:gen-class)
  (:use [carica.core])
  (:require 
   [clojure.data.json :as json]
   [taoensso.carmine :as car :refer (wcar)]
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]))

(defn encode-message [msg]
  (json/write-str msg))

(defn decode-message [msg]
  (json/read-json msg))

(defn cjbot-queue-name-with-prefix [queue]
  (format "%s.%s" (config :redis :prefix) queue))

