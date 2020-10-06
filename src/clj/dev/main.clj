(ns dev.main
  (:require [books.core :as bc]
            [shadow.cljs.devtools.server :as shadow-server]))

(defn start-dev []
  (shadow-server/start!)
  (bc/start-server))

(defn stop-dev []
  (bc/stop-server))

(defn restart []
  (stop-dev)
  (start-dev))
