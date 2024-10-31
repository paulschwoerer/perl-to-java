package com.sippy.wrapper.parent.response;

import java.util.List;

public record GetTnbListResponse(String faultCode, String faultString, List<Tnb> tnbs) {
    public record Tnb(String tnb, String name, boolean isTnb) {}
}
