(ns cloj-fts.core
  (:require [clojure.string :as string])
  (:gen-class))

;;; Core models and data-structures
(defrecord Document [id text])
(defrecord Posting [doc-id token-freq])
(def inverted-idx (atom {}))
(def doc-map (atom {}))

;;; Creates new tokenizer
(defn create-tokenizer
  "Returns a tokenizer based on specified split regex. eg space chars"
  [split-regex]
  (fn [text]
    (string/split text split-regex)))

;;; Creates new normalizer
(defn create-normalizer
  "Returns a normalizer based on specified conversion eg toLowerCase"
  [conversion]
  (fn [token]
    (conversion token)))

(defprotocol IdGenerator
  "Abstraction unique id generator, default impl in-memory"
  (next-id [_] "Get next id"))

;;; Generates unique auto increment ids in memory
(deftype InMemoryIdGenerator [uid]
  IdGenerator
  (next-id [_] (swap! uid inc)))

;;; Instance of InMemoryIdGenerator
(def in-mem-id-generator (InMemoryIdGenerator. (atom 0)))

(def space-split-regex #" +")
(def to-lowercase-conversion #(.toLowerCase %))

;; Space tokenizer
(def space-tokenizer (create-tokenizer space-split-regex))
;; hyphen tokenizer
(def hyphen-tokenizer (create-tokenizer #"-"))
;; Lowercase normalizer
(def lowercase-normalizer (create-normalizer to-lowercase-conversion))

;; Tokenizer chain
(def tokenizer-chain (list space-tokenizer hyphen-tokenizer))
;; Normalizer chain
(def normalizer-chain (list lowercase-normalizer))

(defn get-tokens
  [text]
  (let [text-list (list text)]
    ;; tokenizer chain process
    (loop [tchain tokenizer-chain input-list text-list]
      (if (empty? tchain)
        input-list
        (recur (rest tchain) (reduce #(concat ((first tchain) %2) %1) [] input-list))
        ))))

(defn apply-normalizations
  [token-list]
  ;; apply sequence of normalization fns to the input seq
  (map #((apply comp normalizer-chain) %) token-list))


(defn id->doc
  "Returns a doc id's doc"
  [doc-id]
  (get @doc-map doc-id))

(defn token->postings
  "Returns postings for the given token"
  [token]
  (get @inverted-idx token))

(defn gen-docid-freq-map-for-search
  "Takes a list of tokens, fetches the maps, merges them with addition of freq
  and then finally sorts by value i.e freq count"
  [tokenlist]
  (sort-by val >
           (reduce #(merge-with + %1 %2) {} (map #(token->postings %) tokenlist)))
  )

(defn id-empty?
  [id]
  (or (empty? id) (nil? (first id)) (= "" (first id))))

(defn get-docs-from-sorted-id-list
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
    get-tokens
    apply-normalizations
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
      :else (swap! inverted-idx #(update-in % [token doc-id] inc))
      ))

(defn apply-tokenlist-for-index
  [token-list doc-id]
  (doseq [token token-list]
    (update-inv-idx token doc-id)))

(defn index-doc
  "Indexes a doc"
  [doc]
  (let [id (:id doc)]
    ;; 1. generates tokens using the tokenizer chain
    ;; 2. Apply normalizations using the norm chain
    ;; 3. update processed tokens to inv-index
    ;; 4. update doc to doc-map
    (apply-tokenlist-for-index
      (->
        doc
        :text
        get-tokens
        apply-normalizations
        )
      id)
    (swap! doc-map assoc id doc)
    (id->doc id)
    ))

(defn create-doc
  [text & id]
  (if (id-empty? id)
    (Document. (next-id in-mem-id-generator) text)
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
