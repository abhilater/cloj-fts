(ns cloj-fts.core
  (:require [clojure.string :as string]
            [cloj-fts.id_generator :as id-gen]
            [cloj-fts.processor :as processor])
  (:gen-class))

;;; Core models and data-structures
(defrecord Document [id text])
(defrecord Posting [doc-id token-freq])
(def inverted-idx (atom {}))
(def doc-map (atom {}))

(defn- id->doc
  "Returns a doc id's doc"
  [doc-id]
  (get @doc-map doc-id))

(defn- token->postings
  "Returns postings for the given token"
  [token]
  (get @inverted-idx token))

(defn- gen-docid-freq-map-for-search
  "Takes a list of tokens, fetches the maps, merges them with addition of freq
  and then finally sorts by value i.e freq count"
  [tokenlist]
  (sort-by val >
           (reduce #(merge-with + %1 %2) {} (map #(token->postings %) tokenlist))))

(defn- id-empty?
  [id]
  (or (empty? id) (nil? (first id)) (= "" (first id))))

(defn- get-docs-from-sorted-id-list
  "Gets the document records from the sorted doc id sequence"
  [doc-id-list]
  (map #(id->doc (first %)) doc-id-list))

(defn search
  "Searches for the documents using inverted index"
  [query]
  ;; 1. Apply tokenization and normalization workflow
  ;; 2. Populate postings list from the tokens
  ;; 3. Populates doc-id score map and sort by freq count desc
  ;; 4. returns sorted documents
  (->
    query
    processor/get-tokens
    processor/apply-normalizations
    gen-docid-freq-map-for-search
    get-docs-from-sorted-id-list
    ))

;;; Best function I have created so far
(defn update-inv-idx
  "Updates in inverted index correct for the given token and doc-id"
  [token doc-id]
    (cond
      (not (contains? @inverted-idx token))
        (swap! inverted-idx assoc token {doc-id 1})
      (not (contains? (get @inverted-idx token) doc-id))
        (swap! inverted-idx assoc token (assoc (get @inverted-idx token) doc-id 1))
      :else (swap! inverted-idx #(update-in % [token doc-id] inc))))

(defn apply-tokenlist-for-index
  [token-list doc-id]
  (doseq [token token-list]
    (update-inv-idx token doc-id)))

(defn delete-inv-idx
  "Deletes inverted index for the given token and doc-id"
  [token doc-id]
  (swap! inverted-idx #(update-in % [token] dissoc doc-id)))

(defn apply-tokenlist-for-index-delete
  [token-list doc-id]
  (doseq [token token-list]
    (delete-inv-idx token doc-id)))

(defn index-doc
  "Indexes a doc. Takes a document object and creates a new index or updates the
  existing index if the document exists"
  [doc]
  (let [id (:id doc)]
    ;;0. If existing doc delete all its previous token associations
    (if (contains? @doc-map id)
      (apply-tokenlist-for-index-delete
        (->
          (id->doc id)
          :text
          processor/get-tokens
          processor/apply-normalizations
          )
        id))
    ;; 1. generate tokens using the tokenizer chain
    ;; 2. Apply normalizations using the norm chain
    ;; 3. update processed tokens to inv-index
    (apply-tokenlist-for-index
      (->
        doc
        :text
        processor/get-tokens
        processor/apply-normalizations
        )
      id)
    ;; 4. update doc to doc-map
    (swap! doc-map assoc id doc)
    ;; 5. return indexed document
    (id->doc id)))

(defn create-doc
  [text & id]
  (if (id-empty? id)
    (Document. (id-gen/next-id id-gen/in-mem-id-generator) text)
    (Document. (first id) text)))


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
  (index-doc (Document. 123 "We are going to build a HUUUUUGEEE wall"))
  (index-doc (Document. 124 "Yes we are"))

  ;;; tests search query
  (search "Yes we ArE")
  (search "YeS")
  (search "Z")
  ;=> ([124 3] [123 2])
  ;=> ([124 1])
  ;=> ()

  ;;; tests update-inv-index
  ;(reset! inverted-idx {"hi" {11 1, 22 2}, "we" {11 3}})
  ;=> {"hi" {11 1, 22 2}, "we" {11 3}}
  ;(update-inv-idx "hi" 11)
  ;=> {"hi" {11 2, 22 2}, "we" {11 3}}
  ;(update-inv-idx "hi" 11)
  ;=> {"hi" {11 3, 22 2}, "we" {11 3}}
  ;@inverted-idx
  ;=> {"hi" {11 3, 22 2}, "we" {11 3}}
  ;(update-inv-idx "we" 11)
  ;=> {"hi" {11 3, 22 2}, "we" {11 4}}
  ;@inverted-idx
  ;=> {"hi" {11 3, 22 2}, "we" {11 4}}
  ;(update-inv-idx "hi" 33)
  ;=> {"hi" {11 3, 22 2, 33 1}, "we" {11 4}}
  ;@inverted-idx
  ;=> {"hi" {11 3, 22 2, 33 1}, "we" {11 4}}
  ;(update-inv-idx "ho" 33)
  ;=> {"hi" {11 3, 22 2, 33 1}, "we" {11 4}, "ho" {33 1}}
  ;@inverted-idx
  ;=> {"hi" {11 3, 22 2, 33 1}, "we" {11 4}, "ho" {33 1}}
  ;(update-inv-idx "ho" 33)
  ;=> {"hi" {11 3, 22 2, 33 1}, "we" {11 4}, "ho" {33 2}}
  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
