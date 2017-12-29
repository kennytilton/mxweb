//--- persistence ------------------------------------------------------

const TODO_LS_PREFIX = "todos-MatrixJS.";

class Todo extends MXStorable {
    constructor(islots) {
        super( Object.assign( { lsPrefix: TODO_LS_PREFIX},
            islots,
            { title: cI(islots.title),
                created: islots.created || Date.now(),
                completed: cI(islots.completed || false)}));
        if (!this.created)
            this.created = null;

        if (!this.title)
            this.title = null;
        else
            console.log('todotitle='+this.title);

        if (this.completed==='booya')
            this.completed = null;
    }
    /* nice try: static storableProperties () {
        return ["title", "completed"].concat(super.storableProperties());
    }*/
    toJSON () {
        return {
            id: this.id,
            created: this.created,
            title:  this.title,
            completed: this.completed,
            deleted:   this.deleted
        };
    }
    static mxLoad() {
        return mkm( null, 'TodoGroup',
            { itemsRaw: cI( MXStorable.loadAllItems( Todo, TODO_LS_PREFIX)
                                        .sort( function(a,b) {
                                            return (a.created < b.created) ? -1 : 1;
                                        }) || []),
            items: cF( function(c) {
                clg('calc items entry');
                return c.mx().itemsRaw.filter( function(td) {
                    return !td.deleted;
                });
            }),
            routeItems: cF( c => c.mx().items
                                        .filter( td => todoRoute.v === 'All'
                                                    || xor( todoRoute.v==='Active', td.completed))
                                        .sort( (a,b) => a.created < b.created ? -1 : 1)),
            empty: cF( c => c.mx().items.length === 0)})
    }
}

//const todoRoute = null;

const todoRoute = cFI( c=> {
    let r = (window.localStorage['getObject'])("todo-matrix.route");
    return r === null ? "All" : r;},
    { observer: (n, md, newv ) =>
        (window.localStorage['setObject'])("todo-matrix.route", newv)});

// const Todos = null;

console.log('loading todos!!!');

const Todos = Todo.mxLoad();

console.log('todos='+Todos);