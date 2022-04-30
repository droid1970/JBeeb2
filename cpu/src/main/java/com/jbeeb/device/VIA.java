package com.jbeeb.device;

import com.jbeeb.util.InterruptSource;
import com.jbeeb.util.ClockListener;
import com.jbeeb.util.StateKey;
import com.jbeeb.util.SystemStatus;

public class VIA extends AbstractMemoryMappedDevice implements ClockListener, InterruptSource {

    private static final int ORB = 0x0;
    private static final int ORA = 0x1;
    private static final int DDRB = 0x2;
    private static final int DDRA = 0x3;
    private static final int T1CL = 0x4;
    private static final int T1CH = 0x5;
    private static final int T1LL = 0x6;
    private static final int T1LH = 0x7;
    private static final int T2CL = 0x8;
    private static final int T2CH = 0x9;
    private static final int SR = 0xa;
    private static final int ACR = 0xb;
    private static final int PCR = 0xc;
    private static final int IFR = 0xd;
    private static final int IER = 0xe;
    private static final int ORAnh = 0xf;
    private static final int TIMER1INT = 0x40;
    private static final int TIMER2INT = 0x20;
    private static final int INT_CA1 = 0x02;
    private static final int INT_CA2 = 0x01;
    private static final int INT_CB1 = 0x10;
    private static final int INT_CB2 = 0x08;

    @StateKey(key = "ora")
    protected int ora;

    @StateKey(key = "orb")
    protected int orb;

    @StateKey(key = "ira")
    protected int ira;

    @StateKey(key = "irb")
    protected int irb;

    @StateKey(key = "ddra")
    protected int ddra;

    @StateKey(key = "ddrb")
    protected int ddrb;

    @StateKey(key = "sr")
    protected int sr;

    @StateKey(key = "t1l")
    protected int t1l;

    @StateKey(key = "t2l")
    protected int t2l;

    @StateKey(key = "t1c")
    protected int t1c;

    @StateKey(key = "t2c")
    protected int t2c;

    @StateKey(key = "acr")
    protected int acr;

    @StateKey(key = "pcr")
    protected int pcr;

    @StateKey(key = "ifr")
    protected int ifr;

    @StateKey(key = "ier")
    protected int ier;

    @StateKey(key = "t1hit")
    protected boolean t1hit;

    @StateKey(key = "t2hit")
    protected boolean t2hit;

    @StateKey(key = "portAPins")
    protected int portAPins;

    @StateKey(key = "portBPins")
    protected int portBPins;

    @StateKey(key = "ca1")
    protected boolean ca1;

    @StateKey(key = "ca2")
    protected boolean ca2;

    @StateKey(key = "cb1")
    protected boolean cb1;

    @StateKey(key = "cb2")
    protected boolean cb2;

    protected Runnable ca2callback = null;
    protected Runnable cb2callback = null;

    @StateKey(key = "justhit")
    protected int justhit;

    @StateKey(key = "t1_pb7")
    protected int t1_pb7;

    //private final VIA self;

    public VIA(
            final SystemStatus systemStatus,
            final String name,
            final int startAddress,
            final int size
    ) {
        super(systemStatus, name, startAddress, size);
        //this.self = this;
        reset();
    }

    private void reset() {
        ora = 0;
        orb = 0;
        ddra = 0;
        ddrb = 0;
        ifr = 0;
        ier = 0;
        t1c = 0x1fffe;
        t1l = 0x1fffe;
        t2c = 0x1fffe;
        t2l = 0x1fffe;
        t1hit = true;
        t2hit = true;
        acr = 0;
        pcr = 0;
        t1_pb7 = 1;
    }

    @Override
    public void tick() {
        final int cycles = 1;
        justhit = 0;
        int newT1c = t1c - cycles;
        if (newT1c < -2 && t1c > -3) {
            if (!t1hit) {
                ifr |= TIMER1INT;
                updateIFR();
                if (newT1c  == -3) {
                    justhit |= 1;
                }
                t1_pb7 ^= 1;
            }
            if ((this.acr & 0x40) == 0) {
                t1hit = true;
            }
        }
        while (newT1c < -3) {
            newT1c += t1l + 4;
        }
        t1c = newT1c;

        if ((acr & 0x20) == 0) {
            int newT2c = t2c - cycles;
            if (newT2c < -2) {
                if (!t2hit) {
                    ifr |= TIMER2INT;
                    updateIFR();
                    if (newT2c == -3) {
                        justhit |= 2;
                    }
                    t2hit = true;
                }
                newT2c += 0x20000;
            }
            t2c = newT2c;
        }
    }

    @Override
    public boolean isNMI() {
        return false;
    }

    @Override
    public boolean isIRQ() {
        return (ifr & 0x80) != 0;
    }

    private void updateIFR() {
        if ((ifr & ier & 0x7f) != 0) {
            ifr |= 0x80;
        } else {
            ifr &= ~0x80;
        }
    }

    public void drivePortA() {

    }

    public void portAUpdated() {

    }

    public void drivePortB() {

    }

    public void portBUpdated() {

    }

    public void writeRegister(int addr, final int val) {
        addr &=0xF;
        int mode;
        switch (addr) {
            case ORA:
                ifr &= ~INT_CA1;
                if ((pcr & 0x0a) != 0x02) {
                    ifr &= ~INT_CA2;
                }
                updateIFR();

                mode = (pcr & 0x0e);
                if (mode == 8) {
                    setCA2(false);
                } else if (mode == 0x0a) {
                    setCA2(false);
                    setCA2(true);
                }
                // Fall through
            case ORAnh:
                ora = val;
                recalculatePortAPins();
                break;

            case ORB:
                ifr &= ~INT_CB1;
                if ((this.pcr & 0xa0) != 0x20) {
                    // b-em: Not independent interrupt for CB2
                    ifr &= ~INT_CB2;
                }
                updateIFR();

                orb = val;
                recalculatePortBPins();

                mode = (pcr & 0xe0) >>> 4;
                if (mode == 8) { // Handshake mode
                    setCB2(false);
                } else if (mode == 0x0a) { // Pulse mode
                    setCB2(false);
                    setCB2(true);
                }
                break;
            case DDRA:
                ddra = val;
                recalculatePortAPins();
                break;
            case DDRB:
                ddrb = val;
                recalculatePortBPins();
                break;
            case ACR:
                acr = val;
                if (((justhit & 1) != 0) && ((val & 0x40) == 0)) {
                    t1hit = true;
                }
                break;
            case PCR:
                pcr = val;
                if (((val & 0xe) == 0xc)) {
                    setCA2(false);
                } else if ((val & 0x08) != 0) {
                    setCA2(true);
                }

                if (((val & 0xe0) == 0xc0)) {
                    setCB2(false);
                } else if ((val & 0x80) != 0) {
                    setCB2(true);
                }
                break;
            case SR:
                sr = val;
                break;

            case T1LL:
            case T1CL:
                t1l &= 0x1fe00;
                t1l |= (val << 1);
                break;

            case T1LH:
                t1l &= 0x1fe;
                t1l |= (val << 9);
                if ((justhit & 1) == 0) {
                    ifr &= ~TIMER1INT;
                    updateIFR();
                }
                break;

            case T1CH:
                t1l &= 0x1fe;
                t1l |= (val << 9);
                t1c = t1l + 1;
                t1hit = false;
                if ((justhit & 1) == 0) {
                    ifr &= ~TIMER1INT;
                    updateIFR();
                }
                t1_pb7 = 0;
                break;
            case T2CL:
                t2l &= 0x1fe00;
                t2l |= (val << 1);
                break;
            case T2CH:
                t2l &= 0x1fe;
                t2l |= (val << 9);
                t2c = t2l + 1;
                if ((acr & 0x20) != 0) {
                    t2c -= 2;
                }
                if ((justhit & 2) == 0) {
                    ifr &= ~TIMER2INT;
                    updateIFR();
                }
                t2hit = false;
                break;
            case IER:
                if ((val & 0x80) != 0) {
                    ier |= (val & 0x7f);
                } else {
                    ier &= ~(val & 0x7f);
                }
                updateIFR();
                break;

            case IFR:
                ifr &= ~(val & 0x7f);
                if ((justhit & 1) != 0) {
                    ifr |= TIMER1INT;
                }
                if ((justhit & 2) != 0) {
                    ifr |= TIMER2INT;
                }
                updateIFR();
                break;
        }
    }

    @Override
    public int readRegister(int index) {
        int temp;
        switch (index) {
            case ORA:
                ifr &= ~INT_CA1;
                if ((pcr & 0xa) != 0x2) {
                    ifr &= ~INT_CA2;
                }
                updateIFR();
            case ORAnh:
                if ((acr & 1) != 0) {
                    return ira;
                }
                recalculatePortAPins();
                return portAPins;
            case ORB:
                ifr &= ~INT_CB1;
                if ((pcr & 0xa0) != 0x20) {
                    ifr &= ~INT_CB2;
                }
                updateIFR();

                recalculatePortBPins();
                temp = orb & ddrb;
                if ((acr & 2) != 0) {
                    temp |= (irb & ~ddrb);
                } else {
                    temp |= (portBPins & ~ddrb);
                }
                // If PB7 is active, it is mixed in regardless of
                // whether bit 7 is an input or output.
                if ((acr & 0x80) != 0) {
                    temp &= 0x7f;
                    temp |= (t1_pb7 << 7);
                }
                temp = temp & 0xCF;
                return temp;
            case DDRA:
                return ddra;
            case DDRB:
                return ddrb;
            case T1LL:
                return ((t1l & 0x1fe) >>> 1) & 0xff;
            case T1LH:
                return (t1l >>> 9) & 0xff;
            case T1CL:
                if ((justhit & 1) == 0) {
                    ifr &= ~TIMER1INT;
                    updateIFR();
                }
                return ((t1c + 1) >>> 1) & 0xff;

            case T1CH:
                return ((t1c + 1) >>> 9) & 0xff;

            case T2CL:
                if ((justhit & 2) == 0) {
                    ifr &= ~TIMER2INT;
                    updateIFR();
                }
                return ((t2c + 1) >>> 1) & 0xff;
            case T2CH:
                return ((t2c + 1) >>> 9) & 0xff;

            case SR:
                return sr;
            case ACR:
                return acr;
            case PCR:
                return pcr;
            case IER:
                return ier | 0x80;
            case IFR:
                return ifr;
            default:
                return 0;
                //throw new IllegalStateException(index + ": register index uot of bounds");
        }
    }

    public void recalculatePortAPins() {
        portAPins = ora & ddra;
        portAPins |= ~ddra & 0xFF;
        drivePortA();
        portAUpdated();
    }

    public void recalculatePortBPins() {
        portBPins = orb & ddrb;
        portBPins |= ~ddrb & 0xFF;
        drivePortB();
        portBUpdated();
    }

    public void setCA1(boolean level) {
        if (level == ca1) {
            return;
        }
        boolean pcrSet = (pcr & 1) != 0;
        if (pcrSet == level) {
            if ((acr & 1) != 0) {
                ira = portAPins;
            }
            ifr |= INT_CA1;
            updateIFR();
            if ((pcr & 0xc) == 0x8) {
                setCA2(true);
            }
        }
        ca1 = level;
    }

    public void setCA2(boolean level) {
        if (level == ca2) {
            return;
        }
        ca2 = level;
        boolean output = (pcr & 0x08) != 0;
        if (ca2callback != null) {
            // TODO
        }
        if (output) {
            return;
        }

        boolean pcrSet = (pcr & 4) != 0;
        if (pcrSet == level) {
            ifr |= INT_CA2;
            updateIFR();
        }
    }

    public void setCB1(boolean level) {
        if (level == cb1) {
            return;
        }
        boolean pcrSet = (pcr & 0x10) != 0;
        if (pcrSet == level) {
            if ((acr & 2) != 0) {
                irb = portBPins;
            }
            ifr |= INT_CB1;
            updateIFR();
            if ((pcr & 0xc0) == 0x80) {
                setCB2(true);
            }
        }
        cb1 = level;
    }

    public void setCB2(boolean level) {
        if (level == cb2) {
            return;
        }

        cb2 = level;
        boolean output = (pcr & 0x80) != 0;
        if (cb2callback != null) {
            // TODO
        }
        if (output) {
            return;
        }
        boolean pcrSet = (pcr & 0x40) != 0;
        if (pcrSet == level) {
            ifr |= INT_CB2;
            updateIFR();
        }
    }
}
