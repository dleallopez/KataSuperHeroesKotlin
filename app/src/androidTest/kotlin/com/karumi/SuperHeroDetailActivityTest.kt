package com.karumi

import android.os.Bundle
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.scrollTo
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.runner.AndroidJUnit4
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.karumi.data.repository.SuperHeroRepository
import com.karumi.domain.model.SuperHero
import com.karumi.matchers.ToolbarMatcher
import com.karumi.ui.view.SuperHeroDetailActivity
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock

@RunWith(AndroidJUnit4::class)
class SuperHeroDetailActivityTest : AcceptanceTest<SuperHeroDetailActivity>(SuperHeroDetailActivity::class.java) {

    @Mock
    lateinit var repository: SuperHeroRepository

    override val testDependencies = Kodein.Module(allowSilentOverride = true) {
        bind<SuperHeroRepository>() with instance(repository)
    }

    @Test
    fun showsProperNameGivenSuperHero() {
        val superHero = givenSuperHero()

        startActivityWithSuperHero(superHero)

        onView(
                allOf(
                        withId(R.id.tv_super_hero_name),
                        withText(superHero.name)

                ))
                .check(matches(isDisplayed()))
    }

    @Test
    fun showsNameInToolbarGivenSuperHero() {
        val superHero = givenSuperHero()

        startActivityWithSuperHero(superHero)

        ToolbarMatcher.onToolbarWithTitle(superHero.name)
    }

    @Test
    fun showsBadgeGivenAvenger() {
        val superHero = givenSuperHero(isAvenger = true)

        startActivityWithSuperHero(superHero)

        onView(withId(R.id.iv_avengers_badge)).check(matches(isDisplayed()))
    }

    @Test
    fun hidesBadgeGivenSuperHeroThatIsNoAvenger() {
        val superHero = givenSuperHero()

        startActivityWithSuperHero(superHero)

        onView(withId(R.id.iv_avengers_badge)).check(matches(not(isDisplayed())))
    }

    @Test
    fun showsDescriptionGivenSuperHeroWithShortDescription() {
        val superHero = givenSuperHero(isAvenger = false, descriptionLength = DescriptionLength.SHORT)

        startActivityWithSuperHero(superHero)
        scrollToView(R.id.tv_super_hero_description)

        onView(
                allOf(
                        withId(R.id.tv_super_hero_description),
                        withText(TEST_DESCRIPTION_SHORT)

                ))
                .check(matches(isDisplayed()))
    }

    @Test
    fun showsDescriptionGivenSuperHeroWithVeryLongDescription() {
        val superHero = givenSuperHero(isAvenger = false, descriptionLength = DescriptionLength.VERY_LONG)

        startActivityWithSuperHero(superHero)
        scrollToView(R.id.tv_super_hero_description)

        onView(
                allOf(
                        withId(R.id.tv_super_hero_description),
                        withText(TEST_DESCRIPTION_VERY_LONG)

                ))
                .check(matches(isDisplayed()))
    }

    @Test
    fun hidesProgressGivenSuperHero() {
        val superHero = givenSuperHero()

        startActivityWithSuperHero(superHero)

        onView(withId(R.id.progress_bar)).check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun showsErrorGivenSuperHeroDoesNotExist(){
        givenSuperHeroDoesNotExist()

        startActivityWithSuperHero(null)

        onView(withId(android.support.design.R.id.snackbar_text))
                .check(matches(withText("Superhero not found!")))
    }

    private fun givenSuperHeroDoesNotExist() {
        whenever(repository.getByName(anyString())).thenReturn(null)
    }

    private fun givenSuperHero(isAvenger: Boolean = false,
                               withPicture: Boolean = true,
                               descriptionLength: DescriptionLength = DescriptionLength.SHORT): SuperHero {
        val superHero = SuperHero(
                name = when {
                    isAvenger -> AVENGER_NAME
                    else -> NO_AVENGER_NAME
                },
                photo = when {
                    withPicture -> TEST_PICTURE
                    else -> null
                },
                description = when (descriptionLength) {
                    DescriptionLength.SHORT -> TEST_DESCRIPTION_SHORT
                    DescriptionLength.LONG -> TEST_DESCRIPTION_LONG
                    DescriptionLength.VERY_LONG -> TEST_DESCRIPTION_VERY_LONG
                },
                isAvenger = isAvenger
        )

        whenever(repository.getByName(superHero.name)).thenReturn(superHero)

        return superHero
    }

    private fun startActivityWithSuperHero(superHero: SuperHero?): SuperHeroDetailActivity {
        return startActivity(Bundle().apply {
            putString("super_hero_name_key", superHero?.name ?: NON_EXISTING_NAME)
        })
    }

    private fun scrollToView(viewId: Int) {
        onView(withId(viewId)).perform(scrollTo())
    }
}

enum class DescriptionLength { SHORT, LONG, VERY_LONG }

private const val TEST_PICTURE = "https://i.annihil.us/u/prod/marvel/i/mg/9/b0/537bc2375dfb9.jpg"
private const val AVENGER_NAME = "Avenger"
private const val NO_AVENGER_NAME = "No Avenger"

private const val NON_EXISTING_NAME = "NoAvengerWithThisName"

private const val TEST_DESCRIPTION_SHORT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer vestibulum dui magna."
private const val TEST_DESCRIPTION_LONG = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas at vestibulum sapien, eu tempus dolor. Quisque pretium orci at dolor maximus mattis. Suspendisse auctor, urna ut vestibulum euismod, enim nisi consectetur dui, at dictum risus neque ultrices mi. Praesent tincidunt tellus quam. Lorem ipsum dolor sit amet, consectetur adipiscing elit."
private const val TEST_DESCRIPTION_VERY_LONG = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed condimentum vel enim sed consequat. Suspendisse sagittis malesuada eros id vestibulum. Integer nec mi rhoncus, placerat orci nec, pulvinar augue. Praesent facilisis est non sodales maximus. Nullam ligula eros, commodo et ornare in, molestie non mi. Pellentesque nec laoreet arcu. Praesent a pretium velit. Aliquam laoreet a odio vel porttitor. Integer ac sapien hendrerit libero semper ornare. Aliquam erat volutpat. Praesent scelerisque orci orci, nec vehicula ipsum pretium eu. Fusce vel nisl sem. Curabitur eu ipsum a urna tempor elementum. Integer tincidunt est et tellus vulputate porttitor. Cras varius orci justo, eu sollicitudin ex sodales sit amet.\n" +
        "\n" +
        "Sed efficitur porttitor eros, sit amet porttitor elit faucibus sed. Duis eu purus nisi. Interdum et malesuada fames ac ante ipsum primis in faucibus. Aliquam non magna ut libero vulputate imperdiet. Aenean mauris lacus, lobortis sit amet dapibus dapibus, commodo quis odio. Nunc malesuada sagittis massa. Nam bibendum odio non felis semper pellentesque. Nunc vitae nunc quis augue porttitor imperdiet. Maecenas pulvinar augue tortor, nec tincidunt lacus tristique eu. Maecenas iaculis elementum lacus eu iaculis. Integer non molestie est, ac bibendum elit. Curabitur at varius lacus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Sed a molestie arcu. Phasellus consequat neque et tristique sagittis. Donec bibendum nibh vitae egestas eleifend.\n" +
        "\n" +
        "Aliquam egestas lacus vitae velit porta, vel mollis leo tristique. Aenean iaculis eu leo lacinia dignissim. Curabitur diam erat, auctor at imperdiet tristique, tincidunt mollis ipsum. Donec tincidunt, odio nec volutpat pulvinar, ligula neque venenatis nibh, sit amet molestie sem nulla quis erat. Donec fringilla facilisis eros, sed volutpat odio maximus et. Sed egestas lectus dui, vel suscipit nibh consectetur in. Integer eget mollis ante. Suspendisse porttitor lacus vitae massa tempus, ut varius massa aliquam. In hac habitasse platea dictumst. Vivamus vel ultricies lacus, ut mollis enim. Donec condimentum ante id tincidunt posuere. Praesent varius, erat et maximus lobortis, lorem urna facilisis orci, vitae malesuada eros tortor quis lorem.\n" +
        "\n" +
        "Praesent blandit nisi a felis pulvinar semper. Quisque vitae laoreet nisi, nec vulputate quam. Suspendisse luctus leo vitae dictum condimentum. Aenean libero nisi, dignissim et eleifend vel, feugiat sit amet justo. Duis sit amet consectetur nibh. Mauris malesuada et tortor quis egestas. Phasellus fermentum erat enim. Donec molestie tincidunt sodales. Curabitur leo eros, rutrum a vulputate et, malesuada consequat justo. Vivamus pharetra neque lectus, ut eleifend libero ultricies id. Etiam elit enim, volutpat eu tortor et, consequat tempor turpis. Integer rhoncus nisl ipsum, et consequat lectus vestibulum id.\n" +
        "\n" +
        "Nunc quis sollicitudin tellus. Nam mattis eget turpis in elementum. Donec hendrerit mollis orci non fermentum. Sed odio nisi, feugiat et risus ut, posuere viverra tellus. Suspendisse efficitur, turpis at suscipit condimentum, risus eros tincidunt arcu, id ultrices ligula diam at neque. Nulla convallis, erat sed vulputate venenatis, leo purus vehicula lectus, ut interdum nulla mi a diam. Maecenas mollis nunc id turpis rutrum ultricies."