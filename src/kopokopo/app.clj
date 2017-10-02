(ns kopokopo.app
  (:require [pandect.algo.sha1 :refer :all]
            [base64-clj.core :as base64]
            [clojure.string :as string]))

(declare return-base-string get-symmetric-key return-signature)
(defn validate-data
  "I verify authenticity of the request sent to the app"
  [{:keys [service_name business_number transaction_reference internal_transaction_id transaction_timestamp
           transaction_type account_number sender_phone first_name middle_name last_name amount currency signature]}]
  (let [base-string (return-base-string {:service_name service_name
                                         :business_number business_number
                                         :transaction_reference transaction_reference
                                         :internal_transaction_id internal_transaction_id
                                         :transaction_timestamp transaction_timestamp
                                         :transaction_type transaction_type
                                         :account_number account_number
                                         :sender_phone sender_phone
                                         :first_name first_name
                                         :middle_name middle_name
                                         :last_name last_name
                                         :amount amount
                                         :currency currency
                                         :signature signature})
        symmetric-key (get-symmetric-key)
        new-signature (return-signature {:base-string base-string :symmetric-key symmetric-key})]
    (if (= new-signature signature)
      {:status "01" :description "Accepted" :subscriber_message "Thank you {} for your payment of Ksh {}. We value you"}
      {:status "03" :description "Invalid payment"})))

(defn return-base-string
  "I prepare the base string that will be used to calculate the MAC"
  [arg-map]
  (let [ordered-map (into (sorted-map) arg-map)
        my-string (for [[k v] ordered-map]
                    (str (name k) "=" v))
        base-string (string/join "&" my-string)]
    base-string))

(defn get-symmetric-key
  "I return my account's symmetric key that I have stored in a db.
  Use this function if you are not storing the key in the config"
  []
  )

(defn return-signature
  "I calculate the new signature of the of the parameters sent so that I may use is to authenticate the request"
  [{:keys [base-string symmetric-key]}]
  (let [hmac-sha1 (sha1-hmac base-string symmetric-key)
        new-signature (base64/encode hmac-sha1)]
    new-signature))

