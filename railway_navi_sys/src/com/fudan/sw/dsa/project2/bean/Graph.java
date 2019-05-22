package com.fudan.sw.dsa.project2.bean;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * for subway graph
 * @author zjiehang
 *
 */
public class Graph implements Serializable {

    public List<List<Address>> lineSet = new ArrayList<>();//所有地铁线路集合
    public List<List<Integer>> lineSetTime = new ArrayList<>();
    public int totalAddress = 0;  //总站点数量
    public Graph(){
        initGraph();
    }
    //初始化图
    public void initGraph(){
        //创建所有线路，并添加到集合中
        for(int i=1;i<16;i++){
            List<Address> line = new ArrayList<>();
            lineSet.add(line);
        }
        //创建每条线路站点之间运行时间，并添加到集合中
        for(int i=1;i<16;i++){
            List<Integer> lineTime = new ArrayList<>();
            lineSetTime.add(lineTime);
        }

        String address,longitude,latitude;
        File file = new File("C:\\Users\\Administrator\\IdeaProjects\\pjmap\\src\\subway.xlsx");
        if(!file.exists()){
            System.out.println("文件不存在");
            System.exit(0);
        }
        ArrayList<ArrayList<Object>> result = ExcelUtil.readExcel(file);
        int lineNum = 1;  //确定是几号线
        String tmp;
        String tmp2;
        String time;
        String time2;
        String timeSave = "";
        for(int i = 1 ;i < result.size() ;i++){
            if(result.get(i).get(0).toString().equals("nextLine")){
                i++;
                lineNum++;
                continue;
            }
            if(result.get(i).get(0).toString().equals(""))
                continue;
            address = result.get(i).get(0).toString();
            longitude = result.get(i).get(1).toString();
            latitude = result.get(i).get(2).toString();

            //将每个线路上的站点添加到所在线路上
            lineSet.get(lineNum-1).add(new Address(address,longitude,latitude));
            //System.out.println(lineSet.get(line-1).get(0).getAddress());

            //处理站点之间的时间
            if(result.get(i+1).get(0).toString().equals("nextLine"))
                continue;
            if(result.get(i+1).get(3).toString().equals("06:06")){
                if(result.get(i).get(4).toString().equals("06:06")){
                    int k = i;
                    while (result.get(k-1).get(4).toString().equals("06:06")){
                        k--;
                    }
                    tmp = result.get(k-1).get(4).toString();
                }
                else {
                    if (!result.get(i).get(3).toString().equals("06:06")) {
                        timeSave = result.get(i).get(3).toString();
                    }
                    tmp = result.get(i).get(4).toString();
                }
                tmp2 = result.get(i+1).get(4).toString();
            }
            else{
                if(result.get(i).get(3).toString().equals("06:06")){
                    tmp = timeSave;
                }
                else{
                    tmp = result.get(i).get(3).toString();
                }
                tmp2 = result.get(i+1).get(3).toString();
            }
            time = tmp.substring(3,5);
            time2 = tmp2.substring(3,5);
            int realTime = Integer.parseInt(time);
            int realTime2 = Integer.parseInt(time2);
            if(tmp.substring(0,2).equals(tmp2.substring(0,2))){
                lineSetTime.get(lineNum-1).add(realTime2-realTime);
            }
            else{
                lineSetTime.get(lineNum-1).add(realTime2+60-realTime);
            }
        }
        //4号线为环线，设定环衔接处时间
        lineSetTime.get(3).add(3);
        //获取总的站点数量
        for(int i=0;i<15;i++){
            totalAddress += lineSet.get(i).size();
        }
    }
}
