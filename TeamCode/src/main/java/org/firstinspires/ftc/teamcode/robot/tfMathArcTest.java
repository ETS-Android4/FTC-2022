/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.utils.general.maths.TFMathExtension;
import org.firstinspires.ftc.teamcode.utils.general.maths.Vector2f;
import org.firstinspires.ftc.teamcode.utils.momm.MultiOpModeManager;
import org.firstinspires.ftc.teamcode.utils.general.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.general.OrderedEnumHelper;

@Config
@Autonomous(name = "tfMathArcTest", group = "Test")
public class tfMathArcTest extends MultiOpModeManager {
    // Hardware
    private NewNewDrive drive;
    private Servo collectorArm = null;

    // Constants
    public static double r;// = 20;
    public static float x = 10;
    public static float y = 20;
    public static double speedMin = 0.1;
    public static double speedMax = 0.5;
    public static double arcLength;// = 2 * Math.PI * Math.abs(r) * 1.05;
    public static double COLLECTOR_UP = 0.6;
    public static int num = 0;
    
    //vector element array to test arcs in all quadrants in one build & run session; code is needed to increment the individual element indices though
    //Warning: giving it axis aligned vector (points along the axis line itself) it might return a null value in one(or both) element(s) or just return wrong values
    public static float[] arrayX = new float[] {10, -7, 10,  15, 8, -9, -16, -10};
    public static float[] arrayY = new float[] {20, 2, -10, -10, 7, 13, -16, -15};
    public static int index = 5;
    public static int version_control = 1;

    // Members
    private AUTO_STATE state = AUTO_STATE.DONE;
    private final ElapsedTime waitTimer = new ElapsedTime();

    @Override
    public void init() {
        boolean error = false;
        telemetry.addData("Status", "Initializing...");

        try {
            super.register(new NewNewDrive());

            drive = new NewNewDrive();
            super.register(drive);

            telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

            super.init();
        } catch (Exception e) {
            telemetry.log().add(String.valueOf(e));
            error = true;
        }

        try {
            collectorArm = hardwareMap.get(Servo.class, "CollectorArm");
            //collector = hardwareMap.get(DcMotor.class, "Collector");
        } catch (Exception e) {
            telemetry.log().add("Could not find collector");
            error = true;
        }


        // Initialization status
        String status = "Ready";
        if (error) {
            status = "Hardware Error";
        }
        telemetry.addData("Status", status);
        drive.enableLogging();
    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start() {
        super.start();
        num = 0;
        drive.setDoneFalse();
        state = AUTO_STATE.TEST_MOVE;
    }



    @Override
    public void loop() {

        Vector2f targetVector;

        targetVector = new Vector2f(arrayX[5], arrayY[5]);

        double[] f = TFMathExtension.makeArcV1(targetVector);

        r = f[0];
        arcLength = f[1];
        // Step through the auto commands
        switch (state) {
            case TEST_MOVE:
                drive.arcTo(r, arcLength, speedMin, speedMax);
                //drive.combinedCurves(0, 10, speedMin, speedMax, 0, 10, speedMin, speedMax);
                collectorArm.setPosition(COLLECTOR_UP);
                if (drive.isDone() && !drive.isBusy()) {
                    waitTimer.reset();
                    state = AUTO_STATE.TEST_REVERSE;
                }
                break;
            case TEST_REVERSE:
                drive.arcTo(-r, -arcLength, speedMin, speedMax);
                //drive.combinedCurves(0, 10, speedMin, speedMax, 0, 10, speedMin, speedMax);
                collectorArm.setPosition(COLLECTOR_UP);
                if (drive.isDone() && !drive.isBusy()) {
                    waitTimer.reset();
                    state = AUTO_STATE.DONE;
                }
                break;

            // Stop processing
            case DONE:
                break;

        }

        //log what state it currently is in
        telemetry.addData("Auto Step: ", state);
        
        //log the encoder ticks of the motors
        telemetry.addData("Left Ticks: ", drive.leftPos());
        telemetry.addData("Right Ticks: ", drive.rightPos());
        
        //log the velocity of the individual motors
        telemetry.addData("leftVel: ", drive.leftVel());
        telemetry.addData("rightVel: ", drive.rightVel());
        
        //log the input and output of the method
        telemetry.addData("makeArc method version: ", version_control);
        telemetry.addData("Index: ", index);
        telemetry.addData("Input Target Vector: ", targetVector);
        telemetry.addData("Output Arc Radius: ", r);
        telemetry.addData("Output Arc Length: ", arcLength);
        
        //force update telemetry
        telemetry.update();
    }

    @Override
    public void stop() {
        state = AUTO_STATE.DONE;
        super.stop();
    }

    enum AUTO_STATE implements OrderedEnum {
        TEST_MOVE,
        TEST_REVERSE,
        DONE;
        public AUTO_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }
}
