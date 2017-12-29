# MatrixJS [TodoFRP](https://github.com/lynaghk/todoFRP)

An implementation of TodoFRP using [MatrixJS](https://github.com/kennytilton/MatrixJS/tree/master/js/matrixjs), an application of the `Matrix` dataflow library to a lightweight, embedded `Tag` HTML-workalike library. A sister ClojureScript version can be found [here](https://github.com/kennytilton/todoFRP/tree/matrixjs/todo/MatrixCLJS).

## Demo

Here is a [live tryout](https://kennytilton.github.io/TodoFRP/).

## Build

To run the ES6 version yourself, open `index-es6.html` in your favorite ES6-ready browser. 

To run the ES5 version, open `index.html` in your favorite browser. (We have included a Google Closure-compiled version of the app.) To rebuild the ES5 version after experimenting with the JS:
```bash
cd TodoFRP/todo/MatrixJS
./todo-es5
```
Then again, open `index.html`.

## Reactive programming

The Matrix dataflow library drives all aspects of this app: model, view, DOM updates, and persistence.

#### Background
With Matrix, global variables or individual properties of objects can be expressed as so-called `cells`. Cells can be `formulaic`, using standard HLL code to compute their value from other cells which themselves can be formulaic or be so-called `input cells`. Input cells are assigned new values by conventional, imperative code, say, in an event handler. The dataflow engine then propagates each new input value to all affected formulae in a [glitch](https://en.wikipedia.org/wiki/Reactive_programming#Glitches)-free cascade. For efficiency, when a cell recomputes its state and comes up with the same value as before, it does not propagate to its dependents.

We call a population of objects connected by interdependent cell properties a `matrix` in concordance with this definition: "an environment in which something else takes form". With `TodoFRP`, that something else is an interactive do-list. As the matrix recomputes values, so-called `observers` (on-change callbacks) do useful work outside the matrix, in our case persisting Todos to localStorage and making precision DOM updates. They bring the "something else" within the matrix to life, if you will.

The spreadsheet, then, is a better metaphor for a matrix than the streams found explicitly or metaphorically in reactive systems. Harken back to your knowledge of VisiCalc as we review state change in matrix apps: 
* Our application state (the matrix) is at rest;
* imperative HLL assigns a new value to a matrix cell;
* the application state is recomputed only as necessary based on cell-to-cell dependencies; and
* our application comes again to rest. 

Where a given formula touches three or five other cells the developer need not be concerned with *streamthink*:  all state will be available as if statically defined. Hence the common preference in FRP documentation for the spreadsheet metaphor.

#### TodoFRP in Matrix, in depth
On page load, existing Todos are loaded from localStorage into `Todos`, a Matrix-enabled global collection with functionally derived properties for: un-deleted Todos; the subset of Todos matching the current route; and a simple `empty` derived value of convenience to the two page chunks wishing to be hidden when no Todos exist:

```js

/*
  Glossary:
     mx   - *Matrix*        : eg, "mkMx" makes a matrix instance capable of wrapping properties in Cells
     cI   - *cell Input*    : create an "input" cell starting with the value argument, then mutated by procedural code.
     cF   - *cellFormulaic* : create a "formulaic" cell that is evaluated immediately and anew when any used cell changes.
*/

static mxLoad() {
    return mkMx( null, 'TodoGroup',
        { itemsRaw: cI( MXStorable.loadAllItems( Todo, TODO_LS_PREFIX)
                                    .sort( (a,b) => a.created < b.created ? -1 : 1) || []),

            items: cF( c => c.mx().itemsRaw.filter( td => !td.deleted)),

            routeItems: cF( c => c.mx().items
                                    .filter( td => todoRoute.v === 'All'
                                                || xor( todoRoute.v==='Active', td.completed))
                                    .sort( (a,b) => a.created < b.created ? -1 : 1)),

            empty: cF( c => c.mx().items.length === 0)})
}
 ```

Each individual Todo itself is given a Matrix incarnation, with input Cells wrapping the properties of a Todo which might change: `title`, `completed`, and `deleted` (if not yet deleted). (Yes, we extended the spec a bit to delete logically, and do not allow un-delete.) An on-change `observer` defined by the `Tag`-supplied `MXStorable` class (from which individual Todos inherit) rewrites a Todo on any change. 

New Todos are simply stored in localStorage and added imperatively to the `itemsRaw` input cell of the global `Todos` collection. Such additions or any change to an individal Todo property flows to matrix tag elements which map to browser DOM elements. Tag formulae recompute the view as the Todos change, then observers provided by `Tag` automatically pipe new attributes (or added/removed elements) to the DOM.

> Note that the specificity of formulas and their dependencies means DOM updates are naturally held to the logical minimum necessary, obviating the need for excessive DOM updates and even superfluous regeneration and diffing of virtual DOM as in `React`.

One can explore the dataflow most readily by looking for the seventeen or so formulae introduced in `cF` forms in `app.js`. Those formulae run reactively when any other Matrix-enabled value used directly or indirectly through a function call changes. Here, for example, is what we call the "dashboard", the counter and some controls gathered below the list of Todos. These have been lightly annotated to describe the dataflow in and out:

```js
function todoDashboard () {
    return footer({class: "footer",
                    hidden: cF( c => Todos.empty)}, // td.deleted -> tds.items -> tds.empty -> here

        span({ class: "todo-count",
                content: cF(c => { let remCt = Todos.items.filter(todo => !todo.completed).length; // td.completed -> here
                                return `<strong>${remCt}</strong> item${remCt === 1 ? '' : 's'} remaining`;})}),

        ul( { class: "filters", name: "filters"},
            [["All", "#/"], ["Active","#/active"], ["Completed","#/completed"]]
                .map( ([ label, route]) => li({},  a({href: route,
                                                      content: label, // below, router -> todoRoute -> here
                                                      class: cF( c => (todoRoute.v === label) ? "selected":"")})))),

        button("Clear completed",
            { class: "clear-completed",
              hidden: cF(c => Todos.items.filter(todo => todo.completed).length === 0), // tds.items and td.completed -> here
                                     // below, call td.delete which sets the td.deleted input cell to true
              onclick: 'Todos.items.filter( td => td.completed ).map( td => td.delete())'}));
}
```

If the reactivity is hard to see, that is because "subscription" is handled transparently by custom getters, and redecided afresh each time a rule runs: if we *read* a cell *this time* then a dependency is established\*. e.g., The "formula" above for whether the dashboard is `hidden` is simply accessing the `Todos.empty` property/cell, which will return `true` or `false`. The formula for the "remaining" count illustrates better the full HLL expressiveness of formulae.

> \* Corollary: a *lexical* cell reference not actually *read* because of code branching will *not* establish a dependency. On the other hand, non-lexically apparent cell references made in called functions *do* establish dependency. We emphasize this distinction because many FRP systems rely on lexical "lifting"

#### Summary
The TodoFRP matrix describes model and view declaratively with reactive formulae. Observer callbacks build and update the DOM as well as take care of persistence. Standard DOM event handlers relay user gestures from the browser to the matrix by imperative assignment to matrix inputs. All dependencies are computed and recomputed dynamically between properties, minimizing expended energy whether in matrix recalculation or DOM maintenance.

## Routing

FlatIron Director is the routing library. HLL callbacks given to the `Router` update a global `todoRoute` cell, effectively exposing the URL route to matrix dependency. The Cell begins life as formulaic (so as to load the persisted value from localStorage) and then becomes a so-called "input" Cell that can be mutated as the user works the controls. The callbacks specified for the Router simply assign new selections to `todoRoute` with conventional HLL assignment, transparently triggering the reactive flow to the model `Todos` property `routeItems` and then on to the view `UL` Todo list. ie, Neither subscribe nor notify must be manually coded.

## License

Copyright © 2017 [Kenneth Tilton](http://github.com/kennytilton)

Distributed under the MIT Public License.
