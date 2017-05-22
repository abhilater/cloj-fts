(ns cloj-fts.components
  (:require [com.stuartsierra.component :as component]))

(defrecord InMemInvIndex [reverse-index doc-map]
  component/Lifecycle

  (start [component]
    (println "Starting InMemInvIndex...")
    (merge component {:reverse-index (atom {})
                      :doc-map       (atom {})}))

  (stop [component]
    (println "Stopping InMemInvIndex...")
    (do
      (dissoc component :reverse-index)
      (dissoc component :doc-map))))
