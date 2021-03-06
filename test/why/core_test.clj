(ns why.core-test
  (:require [clojure.test :refer :all]
            [why.core :refer [because
                              decide decide-states
                              and or not if* when
                              namely]])
  (:refer-clojure :exclude [and or not when]))

(deftest decide-test
  (testing "becauses decide to themselves"
    (is (= (because true)
           (decide (because true)))))

  (testing "booleans and nil decide to themselves with no reasons"
    (are [bc decidable] (= bc (decide decidable))
      (because true) true
      (because false) false
      (because nil) nil))

  (testing "functions decide to their return values"
    (is (= (because true)
           (decide (constantly true)))))

  (testing "conjunctions decide to the logical conjunction of their contained values"
    (are [bc decidable] (= bc (decide decidable))
      (because true) (and true true)
      (because false) (and true false)
      (because false) (and false true)
      (because false) (and false false)))

  (testing "disjunctions decide to the logical disjunction of their contained values"
    (are [bc decidable] (= bc (decide decidable))
      (because true) (or true true)
      (because true) (or true false)
      (because true) (or false true)
      (because false) (or false false)))

  (testing "negations decide to the negation of their contained value"
    (are [bc decidable] (= bc (decide decidable))
      (because false) (not true)
      (because true)  (not false)
      (because false ["it is raining"]) (not (namely true "it is raining"))))

  (testing "named clauses attach reasons to their contained decidables"
    (are [bc decidable] (= bc (decide decidable))
      (because true [:reason-1]) (namely true :reason-1)
      (because false [[:not :reason-1]]) (namely false :reason-1)
      (because true [:reason-1 :reason-2]) (and (namely true :reason-1)
                                                (namely true :reason-2))
      (because true [:reason-1 :reason-2]) (and (-> true
                                                    (namely :reason-1)
                                                    (namely :reason-2)))))

  (testing "if* branches"
    (are [bc decidable] (= bc (decide decidable))
      (because false) (if* true false true)
      (because true)  (if* true true false)
      (because true [:i-have-an-umbrella :its-raining]) (if* (namely true :its-raining)
                                                             (namely true :i-have-an-umbrella)
                                                             (namely true :i-have-sunglasses))
      (because true [:i-have-sunglasses [:not :its-raining]]) (if* (namely false :its-raining)
                                                                   (namely true :i-have-an-umbrella)
                                                                   (namely true :i-have-sunglasses))))

  (testing "when branches"
    (are [bc decidable] (= bc (decide decidable))
      (because false) (when true false)
      (because true)  (when true true)
      (because true [:i-have-an-umbrella :its-raining]) (when (namely true :its-raining)
                                                              (namely true :i-have-an-umbrella))
      (because false [[:not :its-raining]]) (when (namely false :its-raining)
                                                  (namely true :i-have-an-umbrella))))

  (testing "complex expressions decide correctly"
    (is (= (because true [:reason-1 :all-good])
           (decide (namely
                    (and (constantly true)
                         (namely (or false true) :reason-1))
                    :all-good))))))

(deftest decide-states-test
  (testing "decide-states groups decisions in a map"
    (is (= {:state-1 (because true)
            :state-2 (because false)}
           (decide-states {:state-1 true
                           :state-2 false})))))
