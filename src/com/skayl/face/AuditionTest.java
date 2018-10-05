package com.skayl.face;

import com.skayl.face.xmi.XMIModel;
import com.skayl.face.xmi.XMINode;
import com.skayl.face.xmi.XMIParser;
import java.util.ArrayList;

public class AuditionTest {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        XMIParser parser = new XMIParser();
        
        try {
            parser.load("input.face");
        } catch (Exception e) {
            System.out.println("Exception Loading : " + e.getMessage());
            return;
        }
        
        XMIModel model = parser.getModel();
        ArrayList<XMINode> messages = new ArrayList<>();
        model.findNodesByXMIType("platform:View", messages);
        
        for (XMINode node : messages) {
            System.out.println(node.getAttribute("name"));
        }
        
        XMINode node = model.findNodeById("FMW_64973E55_833E_482F_AEE8_44415A603EE2");
        System.out.println(node.getAttribute("name"));
        System.out.println(node.getXMIType());
        
        node = model.findNodeByAttribute("type", "FMW_6D742E2C_1DA4_43B3_B00E_F3693437F26B");
        System.out.println(node.getAttribute("rolename"));
        System.out.println(node.getXMIType());
        
        // The following call sets up the subtree search test that follows.  The
        // guid used below is a node that is known to have child nodes.
        node = model.findNodeById("FMW_E93487D2_CCF0_4BAF_B8E5_503DCF66FAA0");
        
        // This version of the method is not clearly documented (it was previously
        // a private mtehod), but it takes, as its first parameter, the nod from
        // which to start the search.
        // PS: the two parameter version of this method actually calls this
        //     method using the Root Node as the first parameter.
        node = model.findNodeByAttribute(node, "rolename", "latitude_radians");
        System.out.println(node.getAttribute("rolename"));
    }
}