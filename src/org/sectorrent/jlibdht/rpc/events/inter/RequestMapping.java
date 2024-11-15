package org.sectorrent.jlibdht.rpc.events.inter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {

    String value();

    Priority priority() default Priority.DEFAULT;
}
