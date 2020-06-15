package com.example.eventorganizer;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.platform.app.InstrumentationRegistry;

import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.not;

import static org.junit.Assert.*;

public class UnitTests {

    /**
     * Rule to test {@link MainActivity} activity on emulator.
     */
    @Rule
    public ActivityTestRule<MainActivity> mainActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    /**
     * Testing if application's context is valid (package name).
     */
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.eventorganizer", appContext.getPackageName());
    }

    /**
     * Testing if login field in form has been created properly.
     */
    @Test
    public void ensureLoginFormIsActive() {
        MainActivity mainActivity = mainActivityTestRule.getActivity();
        View loginText = mainActivity.findViewById(R.id.loginText);
        assertThat(loginText, notNullValue());
        assertThat(loginText, instanceOf(EditText.class));
    }

    /**
     * Testing if login form has been passed by server.
     */
    @Test
    public void checkingIfLoginPassed() {
        Espresso.onView(withId(R.id.loginText)).perform(typeText("dziamian"));
        Espresso.onView(withId(R.id.passwordText)).perform(typeText("123"));
        Espresso.closeSoftKeyboard();
        Intents.init();
        Espresso.onView(withId(R.id.loginBtn)).perform(click());
        intended(hasComponent(HomeActivity.class.getName()));
    }

    /**
     * Testing if proper {@link android.widget.Toast} message has appeared (server should not pass login).
     */
    @Test
    public void checkingIfToastMessageIsCorrect() {
        Espresso.onView(withId(R.id.loginText)).perform(typeText("dziamian"));
        Espresso.onView(withId(R.id.passwordText)).perform(typeText("124"));
        Espresso.closeSoftKeyboard();
        Espresso.onView(withId(R.id.loginBtn)).perform(click());
        Espresso.onView(withText("Invalid login or password!")).inRoot(withDecorView(not(mainActivityTestRule.getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));
    }

    /**
     * Testing if {@link android.app.AlertDialog} showed up when there is no connection with server.
     */
    @Test
    public void checkingIfConnectionErrorPopupIsShowed() {
        Espresso.onView(withText("Connection error")).inRoot(isDialog()).check(matches(isDisplayed()));
    }
}
