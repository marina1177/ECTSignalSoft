package mpei.mdobro.diploma.domain.parse;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static java.lang.Math.sqrt;

@Data
//@RequiredArgsConstructor
public class ComplexNumber implements Comparable<ComplexNumber> {

    @NotNull
    private final Double re;
    @NotNull
    private final Double im;

    @NotNull
    @Valid
    Double magnitude;//= getMagnitude();
    @NotNull
    Double phase;//= getPhase();

    ComplexNumber(Double x, Double y) {
        re = x;
        im = y;
        magnitude = Math.sqrt(re * re + im * im);
        phase = sqrt(Math.atan(im / re));
    }
//    private Double getMagnitude() {
//        return Math.sqrt(re * re + im * im);
//    }
//
//    public Double getPhase() {
//        return Math.sqrt(Math.atan(im / re));
//    }

    public static ComplexNumber add(ComplexNumber a, ComplexNumber b) {
        return new ComplexNumber(a.re + b.re, a.im + b.im);
    }

    public ComplexNumber add(ComplexNumber a) {
        return new ComplexNumber(re + a.re, im + a.im);
    }

    public static ComplexNumber multiply(ComplexNumber a, ComplexNumber b) {
        return new ComplexNumber(a.re * b.re - a.im * b.im,
                a.re * b.im + a.im * b.re);
    }

    public ComplexNumber multiply(ComplexNumber a) {
        return new ComplexNumber(re * a.re - im * a.im,
                re * a.im + im * a.re);
    }

    public int compareTo(ComplexNumber p) {

        return magnitude.compareTo(p.magnitude);
    }

    @Override
    public String toString() {
        return "ComplexNumber{" +
                "re=" + re +
                ", im=" + im +
                ", magnitude=" + magnitude +
                ", phase=" + phase +
                '}';
    }
}
