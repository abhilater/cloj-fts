(ns cloj-fts.indexer)


(defn id->doc [index id]
  (@(:doc-map index) id))


(defn token->postings
  "Returns postings for the given token eg

  (token->postings (get-index) \"we\")
  => {123 1, 124 1}"
  [index token]
  (@(:reverse-index index) token))


(defn- update-inv-idx
  "Updates the inverted index correctly for the given token and doc-id"
  [index token doc-id]
  (cond
    (not (contains? @(:reverse-index index) token))
      (swap! (:reverse-index index) assoc token {doc-id 1})
    (not (contains? (@(:reverse-index index) token) doc-id))
      (swap! (:reverse-index index) update-in [token doc-id] (fn [val] 1))
    :else (swap! (:reverse-index index) update-in [token doc-id] inc)))


(defn- delete-index
  "Deletes inverted index for the given token and doc-id"
  [index token doc-id]
  (swap! (:reverse-index index) #(update-in % [token] dissoc doc-id)))


(defn doc-id-map-from-tokens
  "Generates a sorted map of document id and sum of token frequencies for the
  input token list sorted by decreasing frequency sum"
  [index token-seq]
  (sort-by
    val >
    (apply merge-with +
           (map (partial token->postings index) token-seq))))


(defn docs-from-doc-id-map
  "Gets the document records from the sorted doc id sequence"
  [index doc-seq]
  (map #(id->doc index (first %)) doc-seq))


(defn index-token-seq
  "Indexes a sequence of tokens for a document"
  [index token-seq doc-id]
  (doseq [token token-seq]
    (update-inv-idx index token doc-id)))


(defn delete-index-for-token-seq
  "Deletes a sequence of tokens for the given document"
  [index token-seq doc-id]
  (doseq [token token-seq]
    (delete-index index token doc-id)))