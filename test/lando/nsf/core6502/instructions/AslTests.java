package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.core6502.CPU;
import lando.nsf.core6502.ExpectedState;

public class AslTests {

    @Test
    public void asl_accumulator() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "LDA #$99", //1001 1001
                    "ASL A",    //1 0011 0010
                    "BRK"),
                ExpectedState.accumAndStatus(
                        0x32,
                        CPU.START_STATUS | CPU.STATUS_C));
    }
    
    @Test
    public void asl_zero_page() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "LDA #$99", //1001 1001
                    "STA $04",
                    "ASL $04",  //1 0011 0010
                    "BRK"),
                ExpectedState.statusAndMem(
                        CPU.START_STATUS | CPU.STATUS_C,
                        0x04, 0x32));
    }
}
