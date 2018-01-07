package com.github.lessjava.types.inference.impl;

import com.github.lessjava.types.inference.HMType;

public abstract class HMTypeCollection extends HMTypeObject {
    public HMType elementType;
    public String collectionType;
    public String toString;

    public HMTypeCollection(String className, HMType elementType) {
        super(className);
        this.elementType = elementType;
    }

    @Override
    public HMTypeCollection clone() {
        switch (collectionType) {
            case "List":
                return new HMTypeList(this.elementType);
            case "Set":
                return new HMTypeSet(this.elementType);
            //case "Map":
                //return new HMTypeMap(this.elementType);
        }

        return null;
    }
}
