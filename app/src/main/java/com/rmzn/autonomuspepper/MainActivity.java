package com.rmzn.autonomuspepper;


import android.os.Bundle;
import android.widget.Button;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    // The button_switch_autonomous used to toggle the abilities.
    private Button buttonSwitchAutonomous;
    // A boolean used to store the abilities status.
    private boolean abilitiesHeld = false;
    // The holder for the abilities
    private Holder holder; // Holder'ı sınıf düzeyinde tanımlayın.
    // The QiContext provided by the QiSDK.
    private QiContext qiContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        setContentView(R.layout.activity_main);
        QiSDK.register(this, this);
        // Find the button_switch_autonomous in the view.
        buttonSwitchAutonomous = (Button) findViewById(R.id.button_switch_autonomous);

// Set the buttonSwitchAutonomous onClick listener.
        buttonSwitchAutonomous.setOnClickListener(v -> {
            // Check that the Activity owns the focus.
            if (qiContext != null) {
                toggleAbilities();
            }
        });
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // The robot focus is gained.
        // Store the provided QiContext.
        this.qiContext = qiContext;
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.
        // Remove the QiContext.
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }


    private void holdAbilities(QiContext qiContext) {
        // Build the holder for the abilities.
        holder = HolderBuilder.with(qiContext)
                .withAutonomousAbilities(
                        AutonomousAbilitiesType.BACKGROUND_MOVEMENT,
                        AutonomousAbilitiesType.BASIC_AWARENESS,
                        AutonomousAbilitiesType.AUTONOMOUS_BLINKING
                )
                .build();

        // Hold the abilities asynchronously.
        Future<Void> holdFuture = holder.async().hold();

        // Chain the hold with a lambda on the UI thread.
        holdFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            // Store the abilities status.
            abilitiesHeld = true;
            // Change the buttonSwitchAutonomous text.
            buttonSwitchAutonomous.setText("Release");
            // Enable the buttonSwitchAutonomous.
            buttonSwitchAutonomous.setEnabled(true);
        }));
    }
    private void releaseAbilities() {
        // Release the holder asynchronously.
        Future<Void> releaseFuture = holder.async().release();
        // Chain the release with a lambda on the UI thread.
        releaseFuture.andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> {
            // Store the abilities status.
            abilitiesHeld = true;
            // Change the buttonSwitchAutonomous text.
            buttonSwitchAutonomous.setText("Hold");
            // Enable the buttonSwitchAutonomous.
            buttonSwitchAutonomous.setEnabled(true);
        }));
    }
    private void toggleAbilities() {
        // Disable the buttonSwitchAutonomous.
        buttonSwitchAutonomous.setEnabled(false);

        if (abilitiesHeld) {
            releaseAbilities();
        } else {
            holdAbilities(qiContext);
        }
    }
}