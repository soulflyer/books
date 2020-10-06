(ns books.core
  (:require [books.handler :refer [handler]]
            [org.httpkit.server :as server])
  (:gen-class))

(defonce server-pid (atom nil))

;; This is the websockets server
(defn start-server []
  (reset! server-pid (server/run-server #'handler {:port 9091})))

(defn stop-server []
  (when-not (nil? @server-pid)
    (@server-pid)
    (reset! server-pid nil)))

(defn -main [& args]
  (println "Running -main")
  (start-server))
