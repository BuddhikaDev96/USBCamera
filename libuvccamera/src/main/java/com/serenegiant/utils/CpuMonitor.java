
package com.serenegiant.utils;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public final class CpuMonitor {
    private static final String TAG = "CpuMonitor";
    private static final int SAMPLE_SAVE_NUMBER = 10;
    private int[] percentVec = new int[10];
    private int sum3 = 0;
    private int sum10 = 0;
    private long[] cpuFreq;
    private int cpusPresent;
    private double lastPercentFreq = -1.0D;
    private int cpuCurrent;
    private int cpuAvg3;
    private int cpuAvgAll;
    private boolean initialized = false;
    private String[] maxPath;
    private String[] curPath;
    private final CpuMonitor.ProcStat lastProcStat = new CpuMonitor.ProcStat(0L, 0L);
    private final Map<String, Integer> mCpuTemps = new HashMap();
    private int mTempNum = 0;
    private float tempAve = 0.0F;

    public CpuMonitor() {
    }

    private void init() {
        try {
            FileReader fin = new FileReader("/sys/devices/system/cpu/present");

            try {
                BufferedReader rdr = new BufferedReader(fin);
                Scanner scanner = (new Scanner(rdr)).useDelimiter("[-\n]");
                scanner.nextInt();
                this.cpusPresent = 1 + scanner.nextInt();
                scanner.close();
            } catch (Exception var9) {
                Log.e("CpuMonitor", "Cannot do CPU stats due to /sys/devices/system/cpu/present parsing problem");
            } finally {
                fin.close();
            }
        } catch (FileNotFoundException var11) {
            Log.e("CpuMonitor", "Cannot do CPU stats since /sys/devices/system/cpu/present is missing");
        } catch (IOException var12) {
            Log.e("CpuMonitor", "Error closing file");
        }

        this.cpuFreq = new long[this.cpusPresent];
        this.maxPath = new String[this.cpusPresent];
        this.curPath = new String[this.cpusPresent];

        int i;
        for(i = 0; i < this.cpusPresent; ++i) {
            this.cpuFreq[i] = 0L;
            this.maxPath[i] = "/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq";
            this.curPath[i] = "/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq";
        }

        this.lastProcStat.set(0L, 0L);
        this.mCpuTemps.clear();
        this.mTempNum = 0;

        for(i = 0; i < 50; ++i) {
            String path = "/sys/class/hwmon/hwmon" + i;
            File dir = new File(path);
            if (dir.exists() && dir.canRead()) {
                this.mCpuTemps.put(path, 0);
                ++this.mTempNum;
            }
        }

        this.initialized = true;
    }

    public boolean sampleCpuUtilization() {
        long lastSeenMaxFreq = 0L;
        long cpufreqCurSum = 0L;
        long cpufreqMaxSum = 0L;
        if (!this.initialized) {
            this.init();
        }

        for(int i = 0; i < this.cpusPresent; ++i) {
            long cpufreqMax;
            if (this.cpuFreq[i] == 0L) {
                cpufreqMax = this.readFreqFromFile(this.maxPath[i]);
                if (cpufreqMax > 0L) {
                    lastSeenMaxFreq = cpufreqMax;
                    this.cpuFreq[i] = cpufreqMax;
                    this.maxPath[i] = null;
                }
            } else {
                lastSeenMaxFreq = this.cpuFreq[i];
            }

            cpufreqMax = this.readFreqFromFile(this.curPath[i]);
            cpufreqCurSum += cpufreqMax;
            cpufreqMaxSum += lastSeenMaxFreq;
        }

        if (cpufreqMaxSum == 0L) {
            Log.e("CpuMonitor", "Could not read max frequency for any CPU");
            return false;
        } else {
            double newPercentFreq = 100.0D * (double)cpufreqCurSum / (double)cpufreqMaxSum;
            double percentFreq = this.lastPercentFreq > 0.0D ? (this.lastPercentFreq + newPercentFreq) * 0.5D : newPercentFreq;
            this.lastPercentFreq = newPercentFreq;
            CpuMonitor.ProcStat procStat = this.readIdleAndRunTime();
            if (procStat == null) {
                return false;
            } else {
                long diffRunTime = procStat.runTime - this.lastProcStat.runTime;
                long diffIdleTime = procStat.idleTime - this.lastProcStat.idleTime;
                this.lastProcStat.set(procStat);
                long allTime = diffRunTime + diffIdleTime;
                int percent = allTime == 0L ? 0 : (int)Math.round(percentFreq * (double)diffRunTime / (double)allTime);
                percent = Math.max(0, Math.min(percent, 100));
                this.sum3 += percent - this.percentVec[2];
                this.sum10 += percent - this.percentVec[9];

                for(int i = 9; i > 0; --i) {
                    this.percentVec[i] = this.percentVec[i - 1];
                }

                this.percentVec[0] = percent;
                this.cpuCurrent = percent;
                this.cpuAvg3 = this.sum3 / 3;
                this.cpuAvgAll = this.sum10 / 10;
                this.tempAve = 0.0F;
                float tempCnt = 0.0F;
                Iterator var20 = this.mCpuTemps.keySet().iterator();

                while(var20.hasNext()) {
                    String path = (String)var20.next();
                    File dir = new File(path);
                    if (dir.exists() && dir.canRead()) {
                        File file = new File(dir, "temp1_input");
                        if (file.exists() && file.canRead()) {
                            int temp = (int)this.readFreqFromFile(file.getAbsolutePath());
                            this.mCpuTemps.put(path, temp);
                            if (temp > 0) {
                                ++tempCnt;
                                this.tempAve += temp > 1000 ? (float)temp / 1000.0F : (float)temp;
                            }
                        }
                    }
                }

                if (tempCnt > 0.0F) {
                    this.tempAve /= tempCnt;
                }

                return true;
            }
        }
    }

    public int getCpuCurrent() {
        return this.cpuCurrent;
    }

    public int getCpuAvg3() {
        return this.cpuAvg3;
    }

    public int getCpuAvgAll() {
        return this.cpuAvgAll;
    }

    public int getTempNum() {
        return this.mTempNum;
    }

    public int getTemp(int ix) {
        int result = 0;
        if (ix >= 0 && ix < this.mTempNum) {
            String path = "/sys/class/hwmon/hwmon" + ix;
            if (this.mCpuTemps.containsKey(path)) {
                result = (Integer)this.mCpuTemps.get(path);
            }
        }

        return result;
    }

    public float getTempAve() {
        return this.tempAve;
    }

    private long readFreqFromFile(String fileName) {
        long number = 0L;

        try {
            FileReader fin = new FileReader(fileName);

            try {
                BufferedReader rdr = new BufferedReader(fin);
                Scanner scannerC = new Scanner(rdr);
                number = scannerC.nextLong();
                scannerC.close();
            } catch (Exception var12) {
            } finally {
                fin.close();
            }
        } catch (FileNotFoundException var14) {
        } catch (IOException var15) {
            Log.e("CpuMonitor", "Error closing file");
        }

        return number;
    }

    private CpuMonitor.ProcStat readIdleAndRunTime() {
        long runTime = 0L;
        long idleTime = 0L;

        try {
            FileReader fin = new FileReader("/proc/stat");

            Scanner scanner;
            try {
                BufferedReader rdr = new BufferedReader(fin);
                scanner = new Scanner(rdr);
                scanner.next();
                long user = scanner.nextLong();
                long nice = scanner.nextLong();
                long sys = scanner.nextLong();
                runTime = user + nice + sys;
                idleTime = scanner.nextLong();
                scanner.close();
                return new CpuMonitor.ProcStat(runTime, idleTime);
            } catch (Exception var19) {
                Log.e("CpuMonitor", "Problems parsing /proc/stat");
                scanner = null;
            } finally {
                fin.close();
            }
            return null;
        } catch (FileNotFoundException var21) {
            Log.e("CpuMonitor", "Cannot open /proc/stat for reading");
            return null;
        } catch (IOException var22) {
            Log.e("CpuMonitor", "Problems reading /proc/stat");
            return null;
        }
    }

    private static final class ProcStat {
        private long runTime;
        private long idleTime;

        private ProcStat(long aRunTime, long aIdleTime) {
            this.runTime = aRunTime;
            this.idleTime = aIdleTime;
        }

        private void set(long aRunTime, long aIdleTime) {
            this.runTime = aRunTime;
            this.idleTime = aIdleTime;
        }

        private void set(CpuMonitor.ProcStat other) {
            this.runTime = other.runTime;
            this.idleTime = other.idleTime;
        }
    }
}
