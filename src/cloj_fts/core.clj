(ns cloj-fts.core
  (:require [clojure.string :as string]
            [cloj-fts.id_generator :as id-gen]
            [cloj-fts.processor :as processor]
            [cloj-fts.components :as comps]
            [cloj-fts.indexer :as idxr]
            [com.stuartsierra.component :as component])
  (:gen-class))


(defn search
  "Searches for the documents using inverted index
  eg.
  (search (get-index) \"Yes we are\")
  =>({:text \"Yes we Are\", :id \"2eed7d51-7026-4dc2-950a-e77a2b20eb45\"}
     {:text \"We-are going to-build a HUUUUUGEEE wall\", :id \"210dd74a-d25e-4573-b68f-1b738d52bb5e\"})\n

  (search (get-index) \"YeS\")
  => ({:text \"Yes we Are\", :id \"2eed7d51-7026-4dc2-950a-e77a2b20eb45\"})

  (search (get-index) \"YXZ\")
  => ()\n
  "
  [index search-query]
  (->>
    search-query
    processor/tokenize-text
    (idxr/doc-id-map-from-tokens index)
    (idxr/docs-from-doc-id-map index)))


(defn index-doc
  "Indexes a doc. Takes a document object and creates a new index or updates the
  existing index if the document exists eg.
  ;; Inserts>>
  (index-doc (get-index) {:text \"We-are going to-build a HUUUUUGEEE wall\"})
  => {:text \"We-are going to-build a HUUUUUGEEE wall\", :id \"210dd74a-d25e-4573-b68f-1b738d52bb5e\"}

  (index-doc (get-index) {:text \"Yes we Are\"})
  => {:text \"Yes we Are\", :id \"2eed7d51-7026-4dc2-950a-e77a2b20eb45\"}

  ;; Update>>
  (index-doc (get-index) {:id \"2eed7d51-7026-4dc2-950a-e77a2b20eb45\" :text \"No\"})
  => {:id \"2eed7d51-7026-4dc2-950a-e77a2b20eb45\", :text \"No\"}"
  [index doc]
  (let [id (or (:id doc) (str (java.util.UUID/randomUUID)))
        doc (assoc doc :id id)]
    ;;0. If existing doc delete all its previous token associations
    (if (contains? @(:doc-map index) id)
      (idxr/delete-index-for-token-seq index
        (->
          (idxr/id->doc index id)
          :text
          processor/tokenize-text)
        id))
    ;; 1. tokenize text
    ;; 2. update processed tokens to inv-index
    (idxr/index-token-seq index
      (->
        doc
        :text
        processor/tokenize-text)
      id)
    ;; 3. add processed doc to doc-map
    (swap! (:doc-map index) assoc id doc)
    (idxr/id->doc index id)))


(defn start-system
  "Initializes state-ful components and starts the system"
  []
  (let [system (component/system-map
                 :index (comps/map->InMemInvIndex {}))]
    (component/start-system system)))

(def system (start-system))

(defn get-index []
  (:index system))


(defn test-server
  []
  ;(index-doc (create-doc "We are going to build a HUUUUUGEEE wall"))
  {200 "Success!"})

(comment
  ;;; tests the id-generator
  (do
    (future (println (next-id in-mem-id-generator)))
    (future (println (next-id in-mem-id-generator)))
    (future (println (next-id in-mem-id-generator)))
    (future (println (next-id in-mem-id-generator))))

  ;;; tests index-doc
  (index-doc (get-index) {:text "We-are going to-build a HUUUUUGEEE wall"})
  (index-doc (get-index) {:text "That is a HUUUUUGEEE wall"})
  (index-doc (get-index) {:text "Yes we Are"})
  (index-doc (get-index) {:id "dd5062eb-3440-4520-b787-55ae6e214f3f" :text "No"})

  ;;; tests search query
  (search (get-index) "Yes we are")
  (search (get-index) "YeS")
  (search (get-index) "Z")
  )

(defn -main
  [& args]
  "Do nothing"
  )
