# Scimitar IN-DEVELOPMENT

Please check https://sserra.gitbook.io/scimitar/ for full documentation

# Motivation
Scimitar is a small library that leverages annotation processor to simplify ViewModel and ViewModelFactory injection to avoid boilerplate code every time a new view model is needed. Also simplifies asynchronous code state handling.


# Binding view models
To create a ViewModel instance we normally end up doing the same thing over and over again.

```java
mViewModel = ViewModelProviders.of(this, mViewModelFactory)[MyViewModel::class.java]
```
# Exposing async state

Also to expose the state from an async operation from repository level to view level one very common pattern also recommended by Google is to use a ```Resource<T>``` class that has three states: loading, error and success.

Then in an Activity or Fragment we can observer the state like this

```java

mViewModel.liveData.observe(this, Observer { res ->
    res?.let {
        if (it.success()) {
            renderData(it.data)
        } else if (it.error()) {
            renderError()
        } else {
            renderLoading()
        }
    }
})
```
To avoid writing code like these over and over again in large code bases Scimitar uses annotation processing to generate this code in compile.

# Injecting ViewModels
To inject a view model use ```@ViewModel``` annotation

```java 

@ViewModel
lateinit var vm:MyViewModel 
```

Scimitar supports inheritance you can put ```Scimitar.bind(this)``` in a base class and not worry about it anymore, then all you need is to annotate the view models with ```@ViewModel```

# Resource observer

Annotated the methods to run on each state with the following annotations:

```@OnSucess(id="id")```

```@OnError(id="id")```

```@OnLoading(id="id")```

Each annotation takes an identifies to identify the operation running. We can have multiple  resource observer annotated fields.

Lets see an example for fetching a list of users:

```java

@BindViewModel
lateinit var thirdVm: MyViewModel
​
@ResourceObserver(id = "users")
lateinit var usersObserver: StateObserver<User>
​
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
​
    vm.liveData.observe(this, usersObserver)
    vm.getUsers()
}
​
@OnSuccess(id = "users")
fun renderUsers(user: User) {
    Log.d(TAG, "Render user: $user")
}
​
@OnError(id = "users")
fun renderError(t: Throwable) {
    Log.d(TAG, "Show error")
}
​
@OnLoading(id = "users")
fun showLoading() {
    Log.d(TAG, "Show loading")
}

```
