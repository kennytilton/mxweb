(ns tiltontec.webmx.example.testing
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.string :as str]
            [cljs.core.async :refer [<!]]
            [tiltontec.util.core :refer [now]]
            [tiltontec.cell.core :refer-macros [c? c?once] :refer [c-in]]
            [tiltontec.cell.synapse
             :refer-macros [with-synapse]
             :refer []]

            [tiltontec.model.core
             :refer-macros [with-par]
             :refer [fget matrix mx-par md-get md-reset! mxi-find mxu-find-name] :as md]

            [tiltontec.xhr
             :refer [make-xhr send-xhr send-unparsed-xhr xhr-send xhr-await xhr-status
                     xhr-status-key xhr-resolved xhr-error xhr-error? xhrfo synaptic-xhr synaptic-xhr-unparsed
                     xhr-selection xhr-to-map xhr-name-to-map xhr-response]]

            [tiltontec.webmx.gen :refer [evt-tag target-value]
             :refer-macros [h1 h2 h3 h4 h5 section label header footer br
                            textarea p span a img ul li input div button]]
            [tiltontec.webmx.style
             :refer [make-css-inline]
             :as css]
            [goog.dom :as dom]
            [goog.dom.classlist :as classlist]
            [goog.editor.focus :as focus]
            [goog.dom.selection :as selection]
            [goog.events.Event :as event]
            [goog.dom.forms :as form]

            [cljs-http.client :as client]
            [cognitect.transit :as t]
            [clojure.walk :refer [keywordize-keys]]
            [cljs.pprint :as pp]))

(defn test-page-3 []
  [(div {:id      "xx"
         :onclick #(let [me (evt-tag %)]
                     (when true                             ;; (= (:id @me) "xx")
                       (println :xx-click!! % (:id @me) (:clicks @me))
                       (md-reset! me :clicks (inc (:clicks @me)))))}

     {:clicks (c-in 0)}

     (str "Conten ?! Content rule" (md-get (mx-par me) :clicks) "|Ooops")

     (span {:style  (c? (make-css-inline me
                          :color "blue"
                          :background-color "red"
                          :padding (c? (let [c (md-get (mx-par (:tag @me)) :clicks)]
                                         (str (* c 6) "px")))))
            :hidden (c? (odd? (md-get (mx-par me) :clicks)))}
           {:content (c? (str "Himom style ?! " (md-get (mx-par me) :clicks)))})

     (span {:style "color:red;background-color:#eee;padding:10px"}
           {:content (c? (str "Himom style string! " (md-get (mx-par me) :clicks)))})

     (span {:style (c? (let [c (md-get (mx-par me) :clicks)]
                         {:color            "blue"
                          :background-color "yellow"
                          :padding          (str (* c 6) "px")}))}
           {:content (c? (str "Himom style ?! " (md-get (mx-par me) :clicks)))})

     (div
       (input {:id       "subId"
               :tag/type "checkbox"
               :value    "subvalue"
               :checked  (c? (md-get me :subbing?))
               :onclick  #(let [tgt (evt-tag %)]
                            (.stopPropagation %)
                            (md-reset! (evt-tag %) :subbing?
                              (not (md-get me :subbing?))))}
              {:subbing? (c-in true)})
       (label {:for "subId"}
              "Sub label OK?"))

     (div {:class "color-input" :style "margin-top:24px"}
       "Time color: "
       (input {:name     :timecolor
               :class    (c? (let [xx (fget #(= "xx" (md-get % :id)) me)]
                               (assert xx)
                               (when (even? (md-get xx :clicks))
                                 ["back-cyan" "boulder"])))
               :tag/type "text"
               :value    (c-in "#0ff")}))

     (textarea {:cols        40 :rows 5
                :wrap        "hard"
                :placeholder "Tell me a story"
                ;;:value "Four score and seven"
                :autofocus   true}))])

(def ae-adderall "https://api.fda.gov/drug/event.json?search=patient.drug.openfda.brand_name:adderall&limit=1")
(def flickr "https://api.flickr.com/services/rest/?&method=flickr.people.getPublicPhotos&format=json&api_key=6f93d9bd5fef5831ec592f0b527fdeff&user_id=9395899@N08")
(def github "https://api.github.com/")
(def openstmap "http://www.openstreetmap.org/#map=4/38.01/-95.84")
(def mdn-css "https://developer.mozilla.org/en-US/docs/Web/CSS/line-height?raw&section=Summary")
;;;;

;(defn parse-json$
;  ([j$] (parse-json$ j$ true))
;  ([j$ keywordize]
;   (let [r (t/reader :json)]
;     ((if keywordize keywordize-keys identity)
;       (t/read r j$)))))


#_(go (let [r (<! (client/get ae-adderall {:with-credentials? false}))]
        (if (:success r)
          (do
            (prn :body (keys (:body r)) #_(keys (parse-json$ (:body r))))
            (prn :success (:status r) (keys r) (count (:body r))))

          (prn :NO-success :stat (:status r) :ecode (:error-code r) :etext (:error-text r)))))

(def ae-brand
  "https://api.fda.gov/drug/event.json?search=patient.drug.openfda.brand_name:~a&limit=~a")

(def rx-nav-unk
  "https://rxnav.nlm.nih.gov/REST/interaction/interaction.json?rxcui=341248")

(defn evt-std [e]
  (.stopPropagation e)
  (.upgradeDom js/componentHandler))

(defn test-page-4 []

  [(h1 {:class "mdl-typography--display-2"} "Clojure NYC Meet-Up")
   (p {:class "mdl-typography--display-1"} "A Night to Remember")
   (div {:id      "xx"
         :onclick #(let [me (evt-tag %)]
                     (when (= (:id @me) "xx")
                       (println :xx-click!! % (:id @me) (:clicks @me))
                       (md-reset! me :clicks (inc (:clicks @me)))
                       (evt-std %)))}

     {:clicks (c-in 0)
      :brand  "adderall"}

     (do (println :running! (:id @me))
         (str "Content?! Content rule" (md-get me :clicks) "|Ooops"))

     (br)
     (div
       (button {:class "mdl-button mdl-js-button mdl-js-ripple-effect"
                :onclick #(evt-std %)}
               {:mdl true}
               "MDL Rizing")
       (br)
       (let [xx (mx-par me)]
         (println :id?? (:id @me))
         (assert (= "xx" (:id @xx)))
         (when (odd? (md-get xx :clicks))
           (button {:class "mdl-button mdl-js-button mdl-js-ripple-effect"
                    :onclick #(evt-std %)}
                   {:mdl true}
                   "MDL Rizing Dyno"))))
     (br)

     (div {}
         {:ae (c? (with-synapse (:github [])
                    (send-xhr rx-nav-unk #_ ae-adderall)))}
         (p (pp/cl-format "~a adverse event" (md-get me :brand)))
         (when-let [r (xhr-response (md-get me :ae))]
           (str "Booya!:" r)))

     #_ (div (let [ax (with-synapse (:github [])
                      (send-xhr ae-adderall))]
             (if-let [ae (first (:results (:body (xhr-response ax))))]

               [(str "Booya!! " (keys ae))
                (br)(:transmissiondate ae)
                (br)(str (:senderorganization (:sender ae)))
                (br)(str (keys (:patient ae)))
                (br)(str (dissoc (:patient ae) :drug))
                (br)(let [pt (:patient ae)]
                      (str "Age " (:patientonsetage pt) ", gender " (:patientsex pt)))
                (br)(div (for [d (take 3 (:drug (:patient ae)))]
                           (str (keys d))))]
               "No response yet"))))])

(defn matrix-build! []
  (md/make ::startwatch
    :mx-dom (c?once (md/with-par me (test-page-4)))))

(comment
  [ae-count 1
   brand "adderall"
   top (send-xhr :brand-adv-events (cl-format nil ae-brand brand ae-count)
                 {:brand brand
                  :kids  (c? (when-let [aes (:results (xhr-selection me))]
                               (countit :aes aes)
                               (the-kids
                                 (for [ae aes]
                                   (make ::md/family
                                         :name :adverse-event
                                         :ae (select-keys ae [:transmissiondate
                                                              :sender
                                                              :serious])
                                         :patient (dissoc (:patient ae) :drug)
                                         :kids (patient-drugs ae))))))})])