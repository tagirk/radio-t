package su.tagir.apps.radiot.ui.viewmodel

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ViewModelStateTest {

    @Test
    fun completeTest() {
        val viewModelState = ViewModelState.COMPLETE
        assertThat(viewModelState.isCompleted(), `is`(true))
        assertThat(viewModelState.error, `is`(false))
        assertThat(viewModelState.loading, `is`(false))
        assertThat(viewModelState.loadingMore, `is`(false))
        assertThat(viewModelState.refreshing, `is`(false))
        assertThat(viewModelState.getErrorIfNotHandled() == null, `is`(true))
    }

    @Test
    fun errorTest() {
        val viewModelState = ViewModelState.error("Error!")
        assertThat(viewModelState.isCompleted(), `is`(false))
        assertThat(viewModelState.error, `is`(true))
        assertThat(viewModelState.loading, `is`(false))
        assertThat(viewModelState.loadingMore, `is`(false))
        assertThat(viewModelState.refreshing, `is`(false))
        assertThat(viewModelState.getErrorIfNotHandled(), `is`("Error!"))
        assertThat(viewModelState.getErrorIfNotHandled() == null, `is`(true))
    }

    @Test
    fun refreshingTest() {
        val viewModelState = ViewModelState.REFRESHING
        assertThat(viewModelState.isCompleted(), `is`(false))
        assertThat(viewModelState.error, `is`(false))
        assertThat(viewModelState.loading, `is`(false))
        assertThat(viewModelState.loadingMore, `is`(false))
        assertThat(viewModelState.refreshing, `is`(true))
        assertThat(viewModelState.getErrorIfNotHandled() == null, `is`(true))
    }

    @Test
    fun loadMoreTest() {
        val viewModelState = ViewModelState.LOADING_MORE
        assertThat(viewModelState.isCompleted(), `is`(false))
        assertThat(viewModelState.error, `is`(false))
        assertThat(viewModelState.loading, `is`(false))
        assertThat(viewModelState.loadingMore, `is`(true))
        assertThat(viewModelState.refreshing, `is`(false))
        assertThat(viewModelState.getErrorIfNotHandled() == null, `is`(true))
    }

    @Test
    fun loadingTest() {
        val viewModelState = ViewModelState.LOADING
        assertThat(viewModelState.isCompleted(), `is`(false))
        assertThat(viewModelState.error, `is`(false))
        assertThat(viewModelState.loading, `is`(true))
        assertThat(viewModelState.loadingMore, `is`(false))
        assertThat(viewModelState.refreshing, `is`(false))
        assertThat(viewModelState.getErrorIfNotHandled() == null, `is`(true))
    }

}