package com.karumi

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.hasDescendant
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.karumi.data.repository.SuperHeroRepository
import com.karumi.domain.model.SuperHero
import com.karumi.matchers.RecyclerViewItemsCountMatcher
import com.karumi.matchers.ToolbarMatcher
import com.karumi.recyclerview.RecyclerViewInteraction
import com.karumi.ui.presenter.NetworkErrorException
import com.karumi.ui.view.MainActivity
import com.karumi.ui.view.SuperHeroDetailActivity
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

@RunWith(AndroidJUnit4::class)
class MainActivityTest : AcceptanceTest<MainActivity>(MainActivity::class.java) {

    @Mock
    lateinit var repository: SuperHeroRepository

    @Test
    fun showsEmptyCaseIfThereAreNoSuperHeroes() {
        givenThereAreNoSuperHeroes()

        startActivity()

        onView(withText("¯\\_(ツ)_/¯")).check(matches(isDisplayed()))
    }

    @Test
    fun showsToolbarWithProperTitle() {
        val mainActivity = startActivity()

        val toolbarMatcher = ToolbarMatcher

        val expectedTitle = mainActivity.getText(R.string.app_name)
        toolbarMatcher.onToolbarWithTitle(expectedTitle)
    }

    @Test
    fun emptyCaseIsHiddenGivenSomeSuperHeroes() {
        givenSomeSuperHeroes()

        startActivity()

        onView(withId(R.id.tv_empty_case)).check(matches(not(isDisplayed())))
    }

    @Test
    fun progressBarHiddenAfterLoadingSomeSuperHeroes() {
        givenSomeSuperHeroes()

        startActivity()

        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun showsProperNumberOfSuperheroesGivenSomeSuperheroes() {
        givenSomeSuperHeroes()

        startActivity()

        onView(withId(R.id.recycler_view)).check(matches(RecyclerViewItemsCountMatcher.recyclerViewHasItemCount(SOME_SUPERHEROES)))
    }

    @Test
    fun showsProperNameOfSuperheroesGivenALotOfSuperheroes() {
        val superheroes = givenALotOfSuperHeroes()

        startActivity()

        RecyclerViewInteraction.onRecyclerView<SuperHero>(withId(R.id.recycler_view))
                .withItems(superheroes)
                .check { (name), view, exception ->
                    matches(hasDescendant(withText(name))).check(
                            view,
                            exception
                    )
                }
    }

    @Test
    fun showsBadgeForAvengersGivenALotOfSuperheroesWithSomeAvengers() {
        val avengers = givenSuperheroesWithAvengersInEvenPosition(A_LOT_OF_SUPERHEROES)

        startActivity()

        RecyclerViewInteraction.onRecyclerView<SuperHero>(withId(R.id.recycler_view))
                .withItems(avengers)
                .check { (_, _, isAvenger), view, exception ->
                    val itemExpectedVisibility = if (isAvenger) ViewMatchers.Visibility.VISIBLE else ViewMatchers.Visibility.GONE
                    matches(
                            hasDescendant(
                                    allOf(
                                            withId(R.id.iv_avengers_badge),
                                            withEffectiveVisibility(itemExpectedVisibility)
                                    )))
                            .check(
                                    view,
                                    exception
                            )
                }
    }

    @Test
    fun triggersProperIntentWhenTappingOnItem() {
        val superHeroes = givenSomeSuperHeroes()
        val superHeroIndex = 0
        val superHero = superHeroes[superHeroIndex]

        startActivity()

        onView(withId(R.id.recycler_view)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(superHeroIndex, click()))

        intended(hasComponent(SuperHeroDetailActivity::class.java.canonicalName))
        intended(hasExtra("super_hero_name_key", superHero.name))
    }

    @Test
    fun throwsExceptionWhenFetchingSuperHeroesAndNoNetwork() {
        givenNoNetwork()

        startActivity()

        onView(withText("Network error!")).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    private fun givenThereAreNoSuperHeroes() {
        whenever(repository.getAllSuperHeroes()).thenReturn(emptyList())
    }

    private fun givenSomeSuperHeroes(): List<SuperHero> {
        val superheros = mockSuperheroes(SOME_SUPERHEROES)
        whenever(repository.getAllSuperHeroes()).thenReturn(superheros)
        return superheros
    }

    private fun givenALotOfSuperHeroes(): List<SuperHero> {
        val superheroes = mockSuperheroes(A_LOT_OF_SUPERHEROES)
        whenever(repository.getAllSuperHeroes()).thenReturn(superheroes)
        return superheroes
    }

    private fun givenNoNetwork() {
        whenever(repository.getAllSuperHeroes()).thenThrow(NetworkErrorException)
    }

    private fun givenSuperheroesWithAvengersInEvenPosition(size: Int): List<SuperHero> {
        val superheroes = mutableListOf<SuperHero>()
        for (index in 0 until size) {
            superheroes.add(
                    SuperHero(
                            name = "superHero $index",
                            photo = null,
                            isAvenger = index % 2 == 0,
                            description = "description of avenger $index"
                    )
            )
        }
        whenever(repository.getAllSuperHeroes()).thenReturn(superheroes)

        return superheroes
    }

    private fun mockSuperheroes(size: Int, avengers: Boolean = false): List<SuperHero> {
        val superheroes = mutableListOf<SuperHero>()
        for (index in 0 until size) {
            superheroes.add(
                    SuperHero(
                            name = "superHero $index",
                            photo = null,
                            isAvenger = avengers,
                            description = "description of avenger $index"
                    )
            )
        }

        superheroes.forEach {
            whenever(repository.getByName(it.name)).thenReturn(it)
        }

        return superheroes
    }

    override val testDependencies = Kodein.Module(allowSilentOverride = true) {
        bind<SuperHeroRepository>() with instance(repository)
    }
}

private const val SOME_SUPERHEROES = 5
private const val A_LOT_OF_SUPERHEROES = 25