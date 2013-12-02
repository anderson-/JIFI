///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package robotinterface.robot.simulation;
//
//import java.util.Enumeration;
//
//public class TripleRegressionLine {
//
//    //"/\* \d\d\d \*/" regex
//    DataSeries data;
//    double _R;
//    double _slope1;
//    double _slope2;
//    double _slope3;
//    double _yint1;
//    double _yint2;
//    double _yint3;
//    double _x1;
//    double _x2;
//    double _avgSigma;
//
//    public TripleRegressionLine(DataSeries paramDataSeries) {
//        double d5 = 0.0D;
//        double d6 = 0.0D;
//        double d7 = 0.0D;
//
//        double d8 = 0.0D;
//        double d9 = 0.0D;
//        double d10 = 0.0D;
//        double d11 = 0.0D;
//        double d12 = 0.0D;
//
//        this.data = paramDataSeries;
//
//        this.data.sort();
//
//        DataSeries.Point localPoint3 = (DataSeries.Point) this.data.elementAt(this.data.getNumPts() - 1);
//        DataSeries.Point localPoint4 = (DataSeries.Point) this.data.elementAt(0);
//        double d13 = (localPoint3.getX() - localPoint4.getX()) / 3.0D + localPoint4.getX();
//        double d14 = 2.0D * (localPoint3.getX() - localPoint4.getX()) / 3.0D + localPoint4.getX();
//
//        double d1 = nextR(d13, d14);
//
//        TotalResidSumSq localTotalResidSumSq = new TotalResidSumSq(null);
//        localTotalResidSumSq.calculate(d13, d14);
//        this._R = localTotalResidSumSq.getRlines();
//        this._avgSigma = localTotalResidSumSq.getAvgSigma();
//        this._slope1 = localTotalResidSumSq.getB1lines();
//        this._slope2 = localTotalResidSumSq.getB2lines();
//        this._slope3 = localTotalResidSumSq.getB3lines();
//        this._yint1 = localTotalResidSumSq.getYint1();
//        this._yint2 = localTotalResidSumSq.getYint2();
//        this._yint3 = localTotalResidSumSq.getYint3();
//        this._x1 = localTotalResidSumSq.getX1lines();
//        this._x2 = localTotalResidSumSq.getX2lines();
//
//        for (int i = 1; i < this.data.getNumPts() - 4; ++i) {
//            for (int j = i + 2; j < this.data.getNumPts() - 2; ++j) {
//                DataSeries.Point localPoint1 = (DataSeries.Point) this.data.elementAt(i);
//                DataSeries.Point localPoint2 = (DataSeries.Point) this.data.elementAt(j);
//
//                localTotalResidSumSq.calculate(localPoint1.getX(), localPoint2.getX());
//                double d3 = localTotalResidSumSq.getRlines();
//                double d4 = localTotalResidSumSq.getAvgSigma();
//                d11 = localTotalResidSumSq.getX1lines();
//                d12 = localTotalResidSumSq.getX2lines();
//                d5 = localTotalResidSumSq.getB1lines();
//                d6 = localTotalResidSumSq.getB2lines();
//                d7 = localTotalResidSumSq.getB3lines();
//                d8 = localTotalResidSumSq.getYint1();
//                d9 = localTotalResidSumSq.getYint2();
//                d10 = localTotalResidSumSq.getYint3();
//
//                if ((d3 < d1) && (d11 >= this.data.getMinX()) && (d12 <= this.data.getMaxX())) {
//                    if (liesInRectangle(d11, d12, localPoint1.getX(), localPoint2.getX())) {
//                        d13 = d11;
//                        d14 = d12;
//                        d1 = d3;
//                    } else {
//                        DataSeries localDataSeries1 = localTotalResidSumSq.getSubData1();
//                        DataSeries localDataSeries2 = localTotalResidSumSq.getSubData2();
//                        DataSeries localDataSeries3 = localTotalResidSumSq.getSubData3();
//                        double d2 = nextR(localPoint1.getX(), localPoint2.getX(), d3, d5, d6, d7, d11, d12, localDataSeries1, localDataSeries2, localDataSeries3);
//
//                        if (d2 < d1) {
//                            d13 = localPoint1.getX();
//                            d14 = localPoint2.getX();
//                            d1 = d2;
//                            this._R = d3;
//                            this._avgSigma = d4;
//                            this._slope1 = d5;
//                            this._slope2 = d6;
//                            this._slope3 = d7;
//                            this._yint1 = d8;
//                            this._yint2 = d9;
//                            this._yint3 = d10;
//                            this._x1 = d11;
//                            this._x2 = d12;
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private double nextR(double paramDouble1, double paramDouble2) {
//        TotalResidSumSq localTotalResidSumSq = new TotalResidSumSq(null);
//        localTotalResidSumSq.calculate(paramDouble1, paramDouble2);
//        double d2 = localTotalResidSumSq.getB1lines();
//        double d3 = localTotalResidSumSq.getB2lines();
//        double d4 = localTotalResidSumSq.getB3lines();
//        double d5 = localTotalResidSumSq.getX1lines();
//        double d6 = localTotalResidSumSq.getX2lines();
//        double d1 = localTotalResidSumSq.getRlines();
//        DataSeries localDataSeries1 = localTotalResidSumSq.getSubData1();
//        DataSeries localDataSeries2 = localTotalResidSumSq.getSubData2();
//        DataSeries localDataSeries3 = localTotalResidSumSq.getSubData3();
//        return nextR(paramDouble1, paramDouble2, d1, d2, d3, d4, d5, d6, localDataSeries1, localDataSeries2, localDataSeries3);
//    }
//
//    private double nextR(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6, double paramDouble7, double paramDouble8, DataSeries paramDataSeries1, DataSeries paramDataSeries2, DataSeries paramDataSeries3) {
//        double d2 = (paramDouble4 - paramDouble5) * (paramDouble1 - paramDouble7);
//        double d3 = (paramDouble5 - paramDouble6) * (paramDouble2 - paramDouble8);
//        double d4 = 1 / paramDataSeries1.getNumPts() + 1 / paramDataSeries2.getNumPts() + (paramDataSeries1.getXmean() - paramDouble1) * (paramDataSeries1.getXmean() - paramDouble1) / paramDataSeries1.getSxx() + (paramDataSeries2.getXmean() - paramDouble1) * (paramDataSeries2.getXmean() - paramDouble1) / paramDataSeries2.getSxx();
//
//        double d5 = -1 / paramDataSeries2.getNumPts() - ((paramDataSeries2.getXmean() - paramDouble1) * (paramDataSeries2.getXmean() - paramDouble2) / paramDataSeries2.getSxx());
//
//        double d6 = 1 / paramDataSeries2.getNumPts() + 1 / paramDataSeries3.getNumPts() + (paramDataSeries2.getXmean() - paramDouble2) * (paramDataSeries2.getXmean() - paramDouble2) / paramDataSeries2.getSxx() + (paramDataSeries3.getXmean() - paramDouble2) * (paramDataSeries3.getXmean() - paramDouble2) / paramDataSeries3.getSxx();
//
//        double d1 = 1.0D / (d4 * d6 - (d5 * d5)) * (d2 * d2 * d6 - (2.0D * d2 * d3 * d5) + d3 * d3 * d4);
//
//        return (paramDouble3 + d1);
//    }
//
//    private boolean liesInRectangle(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4) {
//        double d1 = 0.0D;
//        double d2 = 0.0D;
//        double d3 = 0.0D;
//        double d4 = 0.0D;
//        double d5 = 0.0D;
//
//        for (Enumeration localEnumeration = this.data.elements(); localEnumeration.hasMoreElements();) {
//            DataSeries.Point localPoint = (DataSeries.Point) localEnumeration.nextElement();
//            if ((localPoint.getX() >= paramDouble3) && (d1 <= paramDouble3)) {
//                d2 = d1;
//                d3 = localPoint.getX();
//            }
//            if ((localPoint.getX() >= paramDouble4) && (d1 <= paramDouble4)) {
//                d4 = d1;
//                d5 = localPoint.getX();
//            }
//            d1 = localPoint.getX();
//        }
//
//        return ((paramDouble1 >= d2) && (paramDouble1 <= d3) && (paramDouble2 >= d4) && (paramDouble2 <= d5));
//    }
//
//    public double getR() {
//        return this._R;
//    }
//
//    public double getAvgSigma() {
//        return this._avgSigma;
//    }
//
//    public double getSlope1() {
//        return this._slope1;
//    }
//
//    public double getYint1() {
//        return this._yint1;
//    }
//
//    public double getSlope2() {
//        return this._slope2;
//    }
//
//    public double getYint2() {
//        return this._yint2;
//    }
//
//    public double getSlope3() {
//        return this._slope3;
//    }
//
//    public double getYint3() {
//        return this._yint3;
//    }
//
//    public double getX1() {
//        return this._x1;
//    }
//
//    public double getX2() {
//        return this._x2;
//    }
//
//    public DataSeries getEndPoints() {
//        double d1 = this.data.getMinX();
//        double d2 = this.data.getMaxX();
//        DataSeries localDataSeries = new DataSeries();
//        localDataSeries.add(d1, getSlope1() * d1 + getYint1());
//        localDataSeries.add(getX1(), getSlope1() * getX1() + getYint1());
//        localDataSeries.add(getX2(), getSlope2() * getX2() + getYint2());
//        localDataSeries.add(d2, getSlope3() * d2 + getYint3());
//        return localDataSeries;
//    }
//
//    public String toString() {
//        DataSeries localDataSeries = getEndPoints();
//        return "TripleRegressionLine:\n   Sum of the three sums-of-squares-of-residuals = " + this._R + "\n" + "   Properties:\n" + "     Slope1 = " + this._slope1 + "\n" + "      Yint1 = " + this._yint1 + "\n" + "     Slope2 = " + this._slope2 + "\n" + "      Yint2 = " + this._yint2 + "\n" + "     Slope3 = " + this._slope3 + "\n" + "      Yint3 = " + this._yint3 + "\n" + "   Endpoints:\n" + "      " + localDataSeries.getX(0) + ", " + localDataSeries.getY(0) + "\n" + "      " + localDataSeries.getX(1) + ", " + localDataSeries.getY(1) + "\n" + "      " + localDataSeries.getX(2) + ", " + localDataSeries.getY(2) + "\n" + "      " + localDataSeries.getX(3) + ", " + localDataSeries.getY(3) + "\n";
//    }
//
//    private class TotalResidSumSq {
//
//        double Rlines;
//        double X1lines;
//        double X2lines;
//        double avgSigma;
//        double B1lines;
//        double B2lines;
//        double B3lines;
//        double yint1;
//        double yint2;
//        double yint3;
//        DataSeries subdata1;
//        DataSeries subdata2;
//        DataSeries subdata3;
//        DataSeries.Point p;
//        SingleRegressionLine line1;
//        SingleRegressionLine line2;
//        SingleRegressionLine line3;
//        private final TripleRegressionLine this$0;
//
//        private TotalResidSumSq() {
//            this.this$0 = this$1;
//        }
//
//        public void calculate(double paramDouble1, double paramDouble2) {
//            this.subdata1 = new DataSeries();
//            this.subdata2 = new DataSeries();
//            this.subdata3 = new DataSeries();
//
//            for (Enumeration localEnumeration = this.this$0.data.elements(); localEnumeration.hasMoreElements();) {
//                this.p = ((DataSeries.Point) localEnumeration.nextElement());
//                if (this.p.getX() <= paramDouble1) {
//                    this.subdata1.add(this.p);
//                } else if ((this.p.getX() > paramDouble1) && (this.p.getX() <= paramDouble2)) {
//                    this.subdata2.add(this.p);
//                } else {
//                    this.subdata3.add(this.p);
//                }
//            }
//            this.line1 = new SingleRegressionLine(this.subdata1);
//            this.line2 = new SingleRegressionLine(this.subdata2);
//            this.line3 = new SingleRegressionLine(this.subdata3);
//            this.B1lines = this.line1.getSlope();
//            this.B2lines = this.line2.getSlope();
//            this.B3lines = this.line3.getSlope();
//            this.yint1 = this.line1.getYint();
//            this.yint2 = this.line2.getYint();
//            this.yint3 = this.line3.getYint();
//            this.Rlines = (this.line1.getR() + this.line2.getR() + this.line3.getR());
//            this.avgSigma = ((this.line1.getR() / (this.subdata1.getNumPts() - 1) + this.line2.getR() / (this.subdata2.getNumPts() - 1) + this.line3.getR() / (this.subdata3.getNumPts() - 1)) / 3.0D);
//
//            if ((this.yint2 == this.yint1) && (this.B1lines == this.B2lines)) {
//                this.X1lines = this.this$0.data.getMinX();
//            } else {
//                this.X1lines = ((this.yint2 - this.yint1) / (this.B1lines - this.B2lines));
//            }
//            if ((this.yint3 == this.yint2) && (this.B2lines == this.B3lines)) {
//                this.X2lines = this.this$0.data.getMaxX();
//            } else {
//                this.X2lines = ((this.yint3 - this.yint2) / (this.B2lines - this.B3lines));
//            }
//        }
//
//        public double getRlines() {
//            return this.Rlines;
//        }
//
//        public double getAvgSigma() {
//            return this.avgSigma;
//        }
//
//        public double getB1lines() {
//            return this.B1lines;
//        }
//
//        public double getB2lines() {
//            return this.B2lines;
//        }
//
//        public double getB3lines() {
//            return this.B3lines;
//        }
//
//        public double getYint1() {
//            return this.yint1;
//        }
//
//        public double getYint2() {
//            return this.yint2;
//        }
//
//        public double getYint3() {
//            return this.yint3;
//        }
//
//        public double getX1lines() {
//            return this.X1lines;
//        }
//
//        public double getX2lines() {
//            return this.X2lines;
//        }
//
//        public DataSeries getSubData1() {
//            return this.subdata1;
//        }
//
//        public DataSeries getSubData2() {
//            return this.subdata2;
//        }
//
//        public DataSeries getSubData3() {
//            return this.subdata3;
//        }
//
//        TotalResidSumSq(TripleRegressionLine 
//             
//            .1 param1)
//     {
//       this(this$1);
//        }
//    }
//}
