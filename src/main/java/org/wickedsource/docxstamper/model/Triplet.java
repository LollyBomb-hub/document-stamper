package org.wickedsource.docxstamper.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class Triplet <F, S, T> {

    private F first;
    private S second;
    private T third;

    public Triplet(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public static <Frst, Scnd, Thrd> Triplet<Frst, Scnd, Thrd> of(Frst first, Scnd second, Thrd third) {
        return new Triplet<>(first, second, third);
    }
}
