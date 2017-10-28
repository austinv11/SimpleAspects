package com.austinv11.aspects;

import com.austinv11.aspects.annotation.Discover;

@Discover
public class TestDiscovery {

    @Discover
    public final String thing = "Hello World";

    @Discover
    public void thing2() {}
}
