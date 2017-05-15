(ns cloj-fts.core-test
  (:require [clojure.test :refer :all]
            [cloj-fts.core :refer :all]))



(deftest test-next-id
  (let [ig in-mem-id-generator]
    (is (not (nil? (next-id ig)))
        )
    ))



(deftest test-search
  (let [query "we ARE"]
    (is (= (search query)
           '()))
    (do (index-doc (create-doc "We are going to build a HUUUUUGEEE wall")))
    (is (= (count (search query))
           1))
    (do (index-doc (create-doc "YES we ARe")))
    (is (= (count (search query))
           2))
    (do (index-doc (create-doc "YES Are a" 2)))
    (is (= (count (search query))
           2))
    (is (= (count (search "yes"))
           1))
    ))



