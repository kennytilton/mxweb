(ns tiltontec.tag.example.todomx-list-item
  (:require [cljs.pprint :as pp]
            [clojure.string :as str]
            [bide.core :as r]
            [taoensso.tufte :as tufte :refer-macros (defnp profiled profile)]

            [tiltontec.util.core :refer [pln xor now]]
            [tiltontec.cell.base :refer [unbound ia-type *within-integrity* *defer-changes*]]
            [tiltontec.cell.core :refer-macros [c? c?+ c?n c?+n c?once] :refer [c-in]]
            [tiltontec.cell.evaluate :refer [not-to-be]]
            [tiltontec.cell.observer :refer-macros [fn-obs]]
            [tiltontec.cell.synapse
             :refer-macros [with-synapse]
             :refer []]


            [tiltontec.model.core :refer [matrix mx-par md-get md-reset!
                                          fget mxi-find mxu-find-class mxu-find-type
                                          kid-values-kids] :as md]
            [tiltontec.tag.html
             :refer [io-read io-upsert io-clear-storage
                     tag-dom-create
                     mxu-find-tag mxu-find-class
                     dom-tag tagfo tag-dom
                     dom-has-class dom-ancestor-by-tag]
             :as tag]

            [tiltontec.xhr
             :refer [make-xhr send-xhr send-unparsed-xhr xhr-send xhr-await xhr-status
                     xhr-status-key xhr-resolved xhr-error xhr-error? xhrfo synaptic-xhr synaptic-xhr-unparsed
                     xhr-selection xhr-to-map xhr-name-to-map xhr-response]]

            [tiltontec.tag.gen
             :refer-macros [section header h1 input footer p a span label ul li div button br]
             :refer [dom-tag evt-tag]]

            [tiltontec.tag.style :refer [make-css-inline]]

            [goog.dom :as dom]
            [goog.dom.classlist :as classlist]
            [goog.editor.focus :as focus]
            [goog.dom.selection :as selection]
            [goog.events.Event :as event]
            [goog.dom.forms :as form]

            [tiltontec.tag.example.todo
             :refer [make-todo td-title td-created bulk-todo
                     td-completed td-due-by td-upsert td-delete! load-all
                     td-id td-toggle-completed!]]
            [cljs-time.coerce :as tmc]
            [clojure.string :as $]))

(declare todo-edit ae-explorer)

(defn todo-list-item [me todo matrix]
  ;;(println :building-li (:title @todo))
  (li {:class   (c? [(when (md-get me :selected?) "chosen")
                     (when (td-completed todo) "completed")])

       :display (c? (if-let [route (md-get matrix :route)]
                      (cond
                        (or (= route "All")
                            (xor (= route "Active")
                                 (td-completed todo))) "block"
                        :default "none")
                      "block"))}
      ;;; custom slots..
      {:todo      todo
       ;; above is also key to identify lost/gained LIs, in turn to optimize list maintenance

       :selected? (c? (some #{todo} (md-get (mxu-find-tag me :ul) :selections)))

       :editing   (c-in false)}

      (div {:class "view"}
        (input {:class   "toggle" ::tag/type "checkbox"
                :checked (c? (not (nil? (td-completed todo))))
                :onclick #(td-toggle-completed! todo)})

        (label {:onclick    (c? (let [selector (mxu-find-tag me :ul)]
                                  #(let [curr-sel (md-get selector :selections)]
                                     (md-reset! selector :selections
                                       (if (some #{todo} curr-sel)
                                         (remove #{todo} curr-sel)
                                         (conj curr-sel todo))))))
                :ondblclick #(let [li-dom (dom/getAncestorByTagNameAndClass
                                            (.-target %) "li")
                                   edt-dom (dom/getElementByClass
                                             "edit" li-dom)]
                               (classlist/add li-dom "editing")
                               (tag/input-editing-start edt-dom (td-title todo)))}
               (td-title todo))

        (input {::tag/type "date"
                :class     "due-by"
                :value     (c?n (when-let [db (td-due-by todo)]
                                  (let [db$ (tmc/to-string (tmc/from-long db))]
                                    (subs db$ 0 10))))
                :style     (c?once (make-css-inline me
                                     :background-color (c? (if-let [due (td-due-by todo)]
                                                             (if-let [clock (mxu-find-class (:tag @me) "std-clock")]
                                                               (let [time-left (- due (md-get clock :clock))]
                                                                 ;; (println :bgc? (td-title todo) due time-left)
                                                                 (cond
                                                                   (neg? time-left) "red"
                                                                   (< time-left (* 24 3600 1000)) "orange"
                                                                   (< time-left (* 2 24 3600 1000)) "yellow"
                                                                   :default "green"))
                                                               cache)))))
                :oninput   #(md-reset! todo :due-by
                              (tmc/to-long
                                (tmc/from-string
                                  (form/getValue (.-target %)))))})

        (button {:class   "destroy"
                 :onclick #(td-delete! todo)})

        (ae-explorer todo))

      (input {:class     "edit"
              :onblur    #(todo-edit % todo)
              :onkeydown #(todo-edit % todo)})))

(defn de-whitespace [s]
  ($/replace s #"\s" ""))

#_
    (remove #{ \n\r} "https://rxnav.nlm.nih.gov/REST/interaction/list.json?
                   rxcuis=861226+1170673+1151366+316051+1738581+315971+854873+901803")

(def ae-by-brand "https://api.fda.gov/drug/event.json?search=patient.drug.openfda.brand_name:~(~a~)&limit=3")

(defn ae-explorer [todo]
  (button {:class "li-show"
           :style (c? (let [aes (xhr-response (md-get me :ae))]
                        (println :aex-aes!!! (td-title todo) aes)
                        (when (not= 200 (:status aes))
                          "display:none")))}

          {:ae (c?+ [:obs (fn-obs
                            (when (not= old unbound)
                              ;;(println :aex-tossing-old-xhr!! old)
                              (not-to-be old)))]
                    (do
                     ;;(println :aex-looking-up!!!! (td-title todo))
                     (send-xhr (pp/cl-format nil ae-by-brand (td-title todo)))))}

          (span {:style "font-size:0.7em;margin:2px;margin-top:0;vertical-align:top"}
                "View Adverse Events")))

(def nih-interactions
  "https://rxnav.nlm.nih.gov/REST/interaction/list.json?rxcuis=~{4242~a~^+~}")

;; zolpidem 1170673, 854873
;; bupropion 1151366
;; sumatriptan 1738581
;; furosemide 315971
;; zolmitriptan 901803
;; adderall 1170620

(defn interaction-explorer [todo]
  (button {:class "li-show"
           :style (c? (if-let [ias (xhr-response (md-get me :nih))]
                        (do
                          (println :nih-inters!!!  (:status ias))
                          (when (not= 200 (:status ias))
                            "display:none"))
                        "display:none"))}
          {:nih (c? (let [rxcuis [1170620 315971]]
                     (println :nih-looking-up!!!! rxcuis)
                     (send-xhr (pp/cl-format nil nih-interactions rxcuis))))}
          (span {:style "font-size:0.7em;margin:2px;margin-top:0;vertical-align:top"}
                "View Interactions")))

(defn todo-edit [e todo]
  (let [edt-dom (.-target e)
        li-dom (dom/getAncestorByTagNameAndClass edt-dom "li")]

    (when (classlist/contains li-dom "editing")
      (let [title (str/trim (form/getValue edt-dom))
            stop-editing #(classlist/remove li-dom "editing")]
        (cond
          (or (= (.-type e) "blur")
              (= (.-key e) "Enter"))
          (do
            (stop-editing)                                  ;; has to go first cuz a blur event will sneak in
            (if (= title "")
              (td-delete! todo)
              (md-reset! todo :title title)))

          (= (.-key e) "Escape")
          ;; this could leave the input field with mid-edit garbage, but
          ;; that gets initialized correctly when starting editing
          (stop-editing))))))