/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skayl.face;

import com.skayl.face.xmi.XMIModel;
import com.skayl.face.xmi.XMINode;
import com.skayl.face.xmi.XMIParser;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author arrastador
 */
public class TSMessageHandler {

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

        Map<String, Map<String, String>> allClasses = new HashMap<>();
        for (XMINode node : messages) {
            Map<String, String> classes = new HashMap<>();
            for (int i = 0; i < node.getNumChildren(); i++) {
                classes.put(node.getChild(i).getAttribute("rolename"), tracePath(node.getChild(i), model, node.getChild(i).getAttribute("path")));
            }
            allClasses.put(node.getAttribute("name"), classes);
        }
        for (Map.Entry<String, Map<String, String>> entry : allClasses.entrySet()) {
            createClassFile(entry.getKey(), entry.getValue());
        }

    }

    public static String tracePath(XMINode node, XMIModel model, String path) {
        String[] temp = path.split("\\.");
        int count = 0;
        int length = 0;

        String[] stepsInPath = new String[temp.length];
        for (String a : temp) {
            if (!a.isEmpty()) {
                stepsInPath[length] = a;
                length++;
            }
        }

        String result = "";
        while (count != length) {

            String type = "";
            if (node.hasAttribute("type")) {
                type = node.getAttribute("type");
            } else if (node.hasAttribute("projectedCharacteristic")) {
                type = node.getAttribute("projectedCharacteristic");
            }
            XMINode newNode = model.findNodeById(type);
            if (newNode.equals(newNode)) {
                for (int i = 0; i < newNode.getNumChildren(); i++) {
                    String name = "";
                    if (newNode.getChild(i).hasAttribute("rolename")) {
                        name = newNode.getChild(i).getAttribute("rolename");
                    }
                    if (newNode.getChild(i).hasAttribute("name")) {
                        name = newNode.getChild(i).getAttribute("name");
                    }

                    if (newNode.getChild(i).hasAttribute("rolename") || newNode.getChild(i).hasAttribute("name")) {
                        if (newNode.getChild(i).hasAttribute("type")) {// && (temp[count].contains(node.getChild(i).getAttribute("name")) || temp[count].contains(node.getChild(i).getAttribute("rolename")))) {
                            for (String step : stepsInPath) {
                                if (step != null) {
                                    if (step.contains(name)) {
                                        node = newNode.getChild(i);
                                    }
                                }
                            }
                        }
                        if (stepsInPath[length - 1].contains(name)) {

                            if (node.hasAttribute("type")) {
                                type = node.getAttribute("type");
                            } else if (node.hasAttribute("projectedCharacteristic")) {
                                type = node.getAttribute("projectedCharacteristic");
                            }
                            XMINode finalNode = model.findNodeById(type);
                            result = checkType(finalNode.getXMIType().split(":")[1]);
                            if (result.toLowerCase().contains("struct")) {
                                buildStruct(finalNode, model);
                                System.out.println("Struct");
                            } else if (result.equals("enum")) {
                                result = buildEnum(finalNode, model);
                                System.out.println("enum");
                            }
                        }
                    }
                }
            }
            count++;

        }
        return result;

    }

    public static String traceStructPath(XMINode node, XMIModel model) {

        String result = checkType(node.getXMIType().split(":")[1]);
        String type = "";
        if (node.hasAttribute("type")) {
            type = node.getAttribute("type");
        }
        for (int i = 0; i < node.getNumChildren(); i++) {
            if (node.getChild(i).hasAttribute("type")) {
                type = node.getChild(i).getAttribute("type");
            } else if (node.hasAttribute("projectedCharacteristic")) {
                type = node.getChild(i).getAttribute("projectedCharacteristic");
            }
            XMINode finalNode = model.findNodeById(type);
            result = checkType(finalNode.getXMIType().split(":")[1]);
            if (result.toLowerCase().contains("struct")) {
                buildStruct(finalNode, model);
            } else if (result.equals("enum")) {
                result = buildEnum(finalNode, model);
            }
        }
        if(node.getNumChildren() == 0){
            XMINode finalNode = model.findNodeById(type);
            result = checkType(finalNode.getXMIType().split(":")[1]);
            if (result.toLowerCase().contains("struct")) {
                buildStruct(finalNode, model);
            } else if (result.equals("enum")) {
                result = buildEnum(finalNode, model);
            }
        }
        return result;
    }

    public static String createPrivateVariable(String varName, String varType) {
        String variable = "\t private " + checkType(varType) + " " + varName + "; ";
        return variable;
    }

    public static String createPublicVariable(String varName, String varType) {
        String variable = "\t public " + checkType(varType) + " " + varName + "; ";
        return variable;
    }

    public static String createGetterVariable(String varName, String varType) {
        String variable = "\t public " + checkType(varType) + " get" + toTitleCase(varName) + "(){ return " + varName + "; }";
        return variable;
    }

    public static String createSetterVariable(String varName, String varType) {
        String variable = "\t public void set" + toTitleCase(varName) + "( " + checkType(varType) + " new" + toTitleCase(varName) + " ){ " + varName + " = new" + toTitleCase(varName) + "; }";
        return variable;
    }

    public static String createClass(String varName, Map<String, String> members) {
        String variable = "public class " + toTitleCase(varName) + "{ \n";
        String memberVars = "";
        String getters = "";
        String setters = "";
        for (Map.Entry<String, String> member : members.entrySet()) {
            if (member.getValue().indexOf("enum") == 2) {
                memberVars = memberVars + member.getValue() + "\n";
            } else {
                memberVars = memberVars + createPrivateVariable(member.getKey(), member.getValue()) + "\n";
                getters = getters + createGetterVariable(member.getKey(), member.getValue()) + "\n";
                setters = setters + createSetterVariable(member.getKey(), member.getValue()) + "\n";
            }
        }
        variable = variable + memberVars + getters + setters + " }";
        System.out.println(variable);
        return variable;
    }

    public static String createStruct(String varName, Map<String, String> members) {
        String variable = "public class " + toTitleCase(varName) + "{ \n";
        String memberVars = "";
        for (Map.Entry<String, String> member : members.entrySet()) {
            if (member.getValue().indexOf("enum") == 2) {
                memberVars = memberVars + member.getValue() + "\n";
            } else {
                memberVars = memberVars + createPublicVariable(member.getKey(), member.getValue()) + "\n";
            }
        }
        variable = variable + memberVars + " }";
        System.out.println(variable);
        return variable;
    }

    public static void buildStruct(XMINode node, XMIModel model) {
        ArrayList<XMINode> messages = new ArrayList<>();

        model.findNodesByXMIType("platform:View", messages);

        Map<String, String> memberVariables = new HashMap<>();
        for (int i = 0; i < node.getNumChildren(); i++) {
            memberVariables.put(node.getChild(i).getAttribute("rolename"), traceStructPath(node.getChild(i), model));
        }
        createStructFile(node.getAttribute("name"), memberVariables);
    }

    public static String buildEnum(XMINode node, XMIModel model) {
        ArrayList<String> messages = new ArrayList<>();
        for (int i = 0; i < node.getNumChildren(); i++) {
            messages.add(node.getChild(i).getAttribute("name"));
        }
        return createEnum(node.getAttribute("name"), messages);
    }

    public static String createEnum(String varName, ArrayList<String> enums) {
        String variable = "\t enum " + toTitleCase(varName) + "{ ";
        boolean first = true;
        for (String num : enums) {
            if (first) {
                variable = variable + num;
                first = false;
            } else {
                variable = variable + ", " + num;
            }
        }
        variable = variable + "; }";
        return variable;
    }

    public static String toTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }

    public static String checkType(String type) {
        if (type.equals("String")) {
            return type;
        } else if (type.equals("ULong") || type.equals("UDouble") || type.equals("UFloat") || type.equals("UShort")) {
            return type.toLowerCase().replaceFirst("u", "");
        } else if (type.toLowerCase().equals("enumeration")) {
            return "enum";
        } else if (type.equals("Double") || type.equals("Float") || type.equals("Char") || type.equals("Short") || type.equals("Boolean") || type.equals("Byte") || type.equals("Int") || type.equals("Long")) {
            return type.toLowerCase();
        } else if (type.toLowerCase().equals("bool")) {
            return "boolean";
        } else {
            return type;
        }
    }

    public static void createClassFile(String fileName, Map<String, String> memberVariables) {
        FileWriter fw = null;
        try {
            System.out.println(fileName + ".class");
            fw = new FileWriter(fileName + ".class");
            fw.write("////This code has been auto generated\n");
            fw.write(createClass(fileName, memberVariables));

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    // This is unrecoverable. Just report it and move on
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Success...");
    }

    public static void createStructFile(String fileName, Map<String, String> memberVariables) {
        FileWriter fw = null;
        try {
            System.out.println(fileName + ".class");
            fw = new FileWriter(fileName + ".class");
            fw.write("////This code has been auto generated\n");
            fw.write(createStruct(fileName, memberVariables));

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    // This is unrecoverable. Just report it and move on
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Success...");
    }

}
