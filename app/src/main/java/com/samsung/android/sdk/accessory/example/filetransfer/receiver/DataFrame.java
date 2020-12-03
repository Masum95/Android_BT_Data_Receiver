package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.util.Log;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DataFrame {

    private List<List<String>> df;
    HashMap<String, Integer> colToIndx;

    public DataFrame() {
    }

    public DataFrame(String[] colList){
        int i = 0;
        colToIndx = new HashMap<String, Integer>();
        for(String col:colList){
            colToIndx.put(col, i);
            i++;
        }
    }

    public void setColumnNames(String[] colList){
        int i = 0;
        colToIndx = new HashMap<String, Integer>();

        for(String col:colList){
            colToIndx.put(col, i);
            i++;
        }
    }

    private void setDF(CSVReader reader){
        df = new java.util.ArrayList<List<String>>();

        while (true) {
            String[] line = new String[0];
            try {
                if ((line = reader.readNext()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            df.add(Arrays.asList(line));
        }
    }

    public void readDataFrameFrom(String path) throws FileNotFoundException {

        try (CSVReader reader = new CSVReader(new FileReader(path))) {
           setDF(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readDataFrameFrom(InputStream csvStream) throws FileNotFoundException {

        try (InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
             CSVReader reader = new CSVReader(csvStreamReader)) {
           setDF(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public List<List<String>> getDataFrame(){
        return df;
    }

    public int getNumRow(){
        return df.size();
    }

    public int getNumCol() {
        return df.get(0).size();
    }

    public List getCol(String colName){
        List<String> colAra = new ArrayList<>();
        int colNo = colToIndx.get(colName);
        for(int row = 0; row < getNumRow(); row++)
        {
            colAra.add(df.get(row).get(colNo));
        }

        List<Float> floatAra = new ArrayList<>();

        try {
            for (int i = 0; i < colAra.size(); i++) {
                floatAra.add(Float.parseFloat(colAra.get(i)));
            }
        } catch (NumberFormatException e) {
            return colAra;

        }
        return floatAra;

    }
}
