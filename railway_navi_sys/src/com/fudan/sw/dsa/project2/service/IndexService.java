package com.fudan.sw.dsa.project2.service;

import com.fudan.sw.dsa.project2.bean.Address;
import com.fudan.sw.dsa.project2.bean.Graph;
import com.fudan.sw.dsa.project2.bean.ReturnValue;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * this class is what you need to complete
 * @author zjiehang
 *
 */
@Service
public class IndexService
{
    //the subway graph
    private static List<Integer> listTime = new ArrayList<>();  //相邻站点所花时间
    private static Graph graph = null;
    private static List<Address> pathStation = new ArrayList<>();
    private static List<Address> hasVisited = new ArrayList<>();//记录已经分析过的站点
    private static  Address startStation;
    private static  Address endStation;
    private List<Address> pathFinal = new ArrayList<>();
    private int timeAssis;
    private static int transferTime = 3;  //设置换乘时间
    private static double time = 0;       //耗时
    private static Address address1,address2;  //步行时距离最短站
    private static List<Address> pathStationTmpStart = new ArrayList<>();
    private static double timeTmpStart = 0;
    private static List<Address> pathStationTmpEnd = new ArrayList<>();
    private static double timeTmpEnd = 0;
    private static List<Address> addresses=new ArrayList<>();
    private static DecimalFormat df = new DecimalFormat("0.00");
    private static double time2;
    private static List<Address> pathSpecialStart = new ArrayList<>();
    private static List<Address> pathSpecialEnd = new ArrayList<>();

    /**
     * create the graph use file
     */
    public void createGraphFromFile(){
/*
		//存储地图对象
		try {
			graph = new Graph();
			//写对象流的对象
			ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream("C:\\Users\\Administrator\\IdeaProjects\\pjmap\\src\\com\\fudan\\sw\\dsa\\project2\\service\\graph"));
			oos.writeObject(graph);                 //将Person对象p写入到oos中
			oos.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		*/
        //如果图未初始化
        if (graph == null) {
            try {
                //写对象流的对象
                ObjectInputStream oos = new ObjectInputStream(new FileInputStream("C:\\Users\\Administrator\\IdeaProjects\\pjmap\\src\\com\\fudan\\sw\\dsa\\project2\\service\\graph"));
                graph = (Graph) oos.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        IndexService service = new IndexService();
//        startStation = new Address("大木桥", "121.460100","	31.200321"
//        );
//        endStation = new Address("上海大学", "121.395344", "31.326202");
//        service.fastest();
//        for (int i = 0; i < service.pathFinal.size(); i++) {
//            System.out.println(service.pathFinal.get(service.pathFinal.size() - 1 - i).getAddress());
//
//        }
    }
    private static final  double EARTH_RADIUS = 6378137;//赤道半径
    private static double rad(double d){
        return d * Math.PI / 180.0;
    }
    public static double getDistance(double lon1,double lat1,double lon2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);
        double s = 2 *Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2)+Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        return s;//单位米
    }
    //驱动方法
    public ReturnValue travelRoute(Map<String, Object> params)
    {
        //清楚上次检索数据
        listTime.clear();
        pathStation.clear();
        hasVisited.clear();
        timeTmpStart = 0;
        pathStationTmpStart.clear();
        pathStationTmpEnd.clear();
        timeTmpEnd = 0;
        pathStationTmpEnd.clear();
        addresses.clear();
        address1 = null;
        address2 = null;
        time = 0;

        String startAddress = 	params.get("startAddress").toString();
        String startLongitude = params.get("startLongitude").toString();
        String startLatitude = params.get("startLatitude").toString();
        String endAddress = params.get("endAddress").toString();
        String endLongitude = params.get("endLongitude").toString();
        String endLatitude = params.get("endLatitude").toString();
        String choose = params.get("choose").toString();

        startStation = new Address(startAddress, startLongitude, startLatitude);
        endStation = new Address(endAddress, endLongitude, endLatitude);

        if(startStation.getAddress().equals(endStation.getAddress())){
            return null;
        }
        //寻找最近站点
        switch (choose)
        {
            case "1":
                //步行最少
                transferTime = 3;
                nearest();
                adjust();
                selectWay(address1,address2,1);
                int stationNum1 = pathStation.size();
                for(int i=0;i<pathStation.size();i++){
                    addresses.add(pathStation.get(stationNum1-1-i));
                }
                setPathEnd();
                break;
            case "2":
                //换乘最少
                nearest();
                adjust();
                transferTime = 1003;
                if(!noTransfer(address1,address2)){
                    selectWay(address1,address2,2);
                }
                int stationNum2 = pathStation.size();
                for(int i=0;i<pathStation.size();i++){
                    addresses.add(pathStation.get(stationNum2-1-i));
                }
                setPathEnd();
                break;
            case "3":
                //时间最短:
                transferTime = 3;
//                IndexService service = new IndexService();
//                service.fastest();
//                int stationNum3 = service.pathFinal.size();
//                for(int i=0;i<stationNum3;i++){
//                    addresses.add(service.pathFinal.get(stationNum3-1-i));
//                }
//                setPathEnd();
                nearest();
                adjust();
                selectWay(address1,address2,3);
                int stationNum3 = pathStation.size();
                for(int i=0;i<pathStation.size();i++){
                    addresses.add(pathStation.get(stationNum3-1-i));
                }
                setPathEnd();
                break;
            default:
                break;
        }
        if(time<1000){
            time=time+timeTmpStart+timeTmpEnd+time2;
        }
        else{
            String tmp = time+"";
            int index = tmp.indexOf(".");
            String tmp2 = tmp.substring(0,index);
            int realTime = Integer.parseInt(tmp2);
            time=time-realTime/1000*1000+timeTmpStart+timeTmpEnd+time2;
        }
        double distance = time2*(5000/60.0);
        ReturnValue returnValue=new ReturnValue();
        returnValue.setStartPoint(startStation);
        returnValue.setEndPoint(endStation);
        returnValue.setSubwayList(addresses);
        returnValue.setMinutes((int)time);
        returnValue.setMinutesForWalk((int)distance);
        //System.out.println(time2);
        return returnValue;
    }
    //求距离方法
    public static double LantitudeLongitudeDist(double lon1, double lat1,double lon2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);

        double radLon1 = rad(lon1);
        double radLon2 = rad(lon2);

        if (radLat1 < 0)
            radLat1 = Math.PI / 2 + Math.abs(radLat1);// south
        if (radLat1 > 0)
            radLat1 = Math.PI / 2 - Math.abs(radLat1);// north
        if (radLon1 < 0)
            radLon1 = Math.PI * 2 - Math.abs(radLon1);// west
        if (radLat2 < 0)
            radLat2 = Math.PI / 2 + Math.abs(radLat2);// south
        if (radLat2 > 0)
            radLat2 = Math.PI / 2 - Math.abs(radLat2);// north
        if (radLon2 < 0)
            radLon2 = Math.PI * 2 - Math.abs(radLon2);// west
        double x1 = EARTH_RADIUS * Math.cos(radLon1) * Math.sin(radLat1);
        double y1 = EARTH_RADIUS * Math.sin(radLon1) * Math.sin(radLat1);
        double z1 = EARTH_RADIUS * Math.cos(radLat1);

        double x2 = EARTH_RADIUS * Math.cos(radLon2) * Math.sin(radLat2);
        double y2 = EARTH_RADIUS * Math.sin(radLon2) * Math.sin(radLat2);
        double z2 = EARTH_RADIUS * Math.cos(radLat2);

        double d = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)+ (z1 - z2) * (z1 - z2));
        //余弦定理求夹角
        double theta = Math.acos((EARTH_RADIUS * EARTH_RADIUS + EARTH_RADIUS * EARTH_RADIUS - d * d) / (2 * EARTH_RADIUS * EARTH_RADIUS));
        double dist = theta * EARTH_RADIUS;
        return dist;
    }
    //比较时间最短路径
    protected  void fastest(){
        ArrayList<Address> addressesList = new ArrayList<>();
        ArrayList<Address> addressesList2 = new ArrayList<>();
        double distanceMax1 = Double.MAX_VALUE;
        double distanceMax2 = Double.MAX_VALUE;
        double tmpStart;
        double tmpEnd;
        //获得周围站点列表
        for(int i=0;i<graph.lineSet.size();i++){
            for (Address address:graph.lineSet.get(i)){
                if(Math.abs(address.getLongitude()-startStation.getLongitude())<0.04&&
                        Math.abs(address.getLatitude()-startStation.getLatitude())<0.02){
                    addressesList.add(address);
                }
                if(Math.abs(address.getLongitude()-endStation.getLongitude())<0.04&&
                        Math.abs(address.getLatitude()-endStation.getLatitude())<0.02){
                    if(!addressesList.contains(address))
                    addressesList2.add(address);
                }
            }
        }
        Double max = Double.MAX_VALUE;
        IndexService indexService;
        //IndexService returnValue = new IndexService();
        for (int i=0;i<addressesList.size();i++){
            for (int j=0;j<addressesList2.size();j++){
                indexService = new IndexService();
//              indexService.startStation=addressesList.get(i);
//              indexService.endStation=addressesList2.get(j);
                //System.out.println(addressesList.get(i).getAddress()+addressesList2.get(j).getAddress());
                //System.out.println(addressesList.get(i).getLongitude()+";;;"+addressesList.get(i).getLatitude());
                indexService.findPath(addressesList.get(i),addressesList2.get(j));
                //System.out.println(indexService.time);
                double dist1= LantitudeLongitudeDist(addressesList.get(i).getLongitude(),addressesList.get(i).getLatitude(),
                        startStation.getLongitude(),startStation.getLatitude());
                //System.out.println(dist1);
                double dist2= LantitudeLongitudeDist(addressesList2.get(j).getLongitude(),addressesList2.get(j).getLatitude(),
                        endStation.getLongitude(),endStation.getLatitude());
                //System.out.println(dist2);
                double time1 = dist1/(5000/60.0);
                double time2 = dist2/(5000/60.0);
                double total = indexService.timeAssis+time1+time2;
                //System.out.println(total);
                if(total<max){
                    max = total;
                    //time = total;
                    //returnValue = indexService;
                    pathFinal.clear();
                    pathFinal.addAll(indexService.pathFinal);
                    time = total;
                }
            }
        }

    }
    //取距离最近的站点
    private void nearest(){
        double startStationLon1 = startStation.getLongitude();
        double startStationLat1 = startStation.getLatitude();
        double endStationLon1 = endStation.getLongitude();
        double endStationLat1 = endStation.getLatitude();
        double distanceEnd = Integer.MAX_VALUE;
        double tmpEnd;
        double distanceStart = Integer.MAX_VALUE;
        double tmpStart;
        Address shortestAddressStart = null;
        Address shortestAddressEnd = null;
        int j=0;
        for(int i=0;i<graph.lineSet.size();i++){
            for (Address address:graph.lineSet.get(i)){
                if(Math.abs(address.getLongitude()-startStationLon1)<0.04&&
                        Math.abs(address.getLatitude()-startStationLat1)<0.02){
                    tmpStart = getDistance(address.getLongitude(),address.getLatitude(),startStationLon1,startStationLat1);
                    if(tmpStart<distanceStart){
                        distanceStart = tmpStart;
                        shortestAddressStart = address;
                    }
                }
                if(Math.abs(address.getLongitude()-endStationLon1)<0.04&&
                        Math.abs(address.getLatitude()-endStationLat1)<0.02){
                    tmpEnd = getDistance(address.getLongitude(),address.getLatitude(),endStationLon1,endStationLat1);
                    if(tmpEnd<distanceEnd){
                        distanceEnd = tmpEnd;
                        shortestAddressEnd = address;
                    }
                }
            }
        }//重置指针
        address1 = shortestAddressStart;
        address2 = shortestAddressEnd;
        String timeWalkTmp = df.format((distanceStart+distanceEnd)/(5000/60.0));
        double timeWalk = (distanceStart+distanceEnd)/(5000/60.0);
        String walkDistance = df.format(distanceEnd+distanceStart);
        time2 = timeWalk;
    }
    //处理分支线路
    private void adjust(){
        //判断分支线路
        int index1 = graph.lineSet.get(10).indexOf(address1);
        int index2 = graph.lineSet.get(10).indexOf(address2);
        int index3 = graph.lineSet.get(9).indexOf(address1);
        int index4 = graph.lineSet.get(9).indexOf(address2);
        if(index1>30&&index2<28){
            for(int i=31;i<=index1;i++){
                pathStationTmpStart.add(graph.lineSet.get(10).get(i));
                timeTmpStart += graph.lineSetTime.get(10).get(i-1);
            }
            address1 = graph.lineSet.get(10).get(27);
        }
        if(index3>27&&index4<24){
            for(int i=28;i<=index3;i++){
                pathStationTmpStart.add(graph.lineSet.get(9).get(i));
                timeTmpStart += graph.lineSetTime.get(9).get(i-1);
            }
            address1 = graph.lineSet.get(9).get(23);
        }
        if(index2>30&&index1<=30){
            for(int i=31;i<=index2;i++){
                pathStationTmpEnd.add(graph.lineSet.get(10).get(i));
                timeTmpEnd += graph.lineSetTime.get(10).get(i-1);
            }
            address2 = graph.lineSet.get(10).get(27);
        }
        if(index4>27&&index3<=27){
            for(int i=28;i<=index4;i++){
                pathStationTmpEnd.add(graph.lineSet.get(9).get(i));
                timeTmpEnd += graph.lineSetTime.get(9).get(i-1);
            }
            address2 = graph.lineSet.get(9).get(23);
        }
        setPathStart();
    }
    //寻找最短路径
    private void findPath(Address address1, Address address2){
        pathStation.clear();
        hasVisited.clear();
        address1.getDistance().clear();
        for(int i =0;i < graph.lineSet.size();i++){
            for (Address s:graph.lineSet.get(i)){
                s.preAddress = null;
            }
        }
        //初始化已访问站点集合
        if(!hasVisited.contains(address1)){
            hasVisited.add(address1);
        }

        //取最小值
        Address shortestDisAddress = null;
        //如果找到目标站点为终点站，则结束操作
        while (true) {
            if(address1.getAddress().equals(address2.getAddress()))break;
            shortestDisAddress = extactMin(address1);
            if(shortestDisAddress.equals(address2))break;
            //Relax
            List<Address> linkedAddress2 = getAllLinkedAddress(shortestDisAddress);
            Address nextAddress;
            for (int i = 0; i < linkedAddress2.size(); i++) {
                nextAddress = linkedAddress2.get(i);
                if (hasVisited.contains(nextAddress)) {
                    continue;
                }
                int shortestPath = address1.getDistance().get(shortestDisAddress) + listTime.get(i);
                if (address1.getDistance().get(nextAddress) != null) {
                    //查询节点距离是否可更新
                    if (address1.getDistance().get(nextAddress) > shortestPath) {
                        nextAddress.preAddress = shortestDisAddress;
                        address1.getDistance().remove(nextAddress);
                        address1.getDistance().put(nextAddress, shortestPath);
                    }
                } else {
                    //未初始化节点距离，进行初始化
                    address1.getDistance().remove(nextAddress);
                    nextAddress.preAddress = shortestDisAddress;
                    address1.getDistance().put(nextAddress, shortestPath);
                }
            }
            hasVisited.add(shortestDisAddress);
            //如果已遍历全部站点
            if (hasVisited.size() == graph.totalAddress) {
                setShortestPath(address2);
                timeAssis = address1.getDistance().get(address2);
               // System.out.println("耗时：" + time + "分钟");
                address1.getDistance().clear();
                break;
            }
        }
        if(pathSpecialEnd.size() != 0) {
            for (int i=0;i<pathSpecialEnd.size();i++)
                pathFinal.add(pathSpecialEnd.get(pathSpecialEnd.size()-1-i));}
        setShortestPath1(shortestDisAddress);
        if(pathSpecialStart.size() != 0) {
            for (int i=0;i<pathSpecialStart.size();i++)
                pathFinal.add(pathSpecialStart.get(i));}
        timeAssis = address1.getDistance().get(address2);
        System.out.println("耗时：" + timeAssis + "分钟");
        address1.getDistance().clear();
    }

    //获取最短路径
    private void selectWay(Address address1,Address address2,int choice){
        //已访问全部站点，结束
        if(hasVisited.size() == graph.totalAddress){
            setShortestPath(address2);
            time = address1.getDistance().get(address2);
            //System.out.println("耗时："+time+"分钟");
            address1.getDistance().clear();
            return;
        }
        //初始化已访问站点集合
        if(!hasVisited.contains(address1)){
            hasVisited.add(address1);
        }
        //初始化起点站到前后站distance集合
        if(address1.getDistance().isEmpty()){
            List<Address> linkedAddress = getAllLinkedAddress(address1);
            address1.getDistance().put(address1,0);
            for(int i=0;i<linkedAddress.size();i++){
                address1.getDistance().put(linkedAddress.get(i),listTime.get(i));
                linkedAddress.get(i).preAddress = address1;
                address1.preAddress = null;
            }
        }
        //获取距起点站最近的一个站，准备进行松弛操作
        Address shortestDisAddress = getShortestPath(address1);
        //System.out.println(shortestDisAddress.getAddress()+";;;;");
        if(choice == 2&&address1.getDistance().get(shortestDisAddress)<1000){   //最少换乘，判断一次换乘是否可以
            for(int i=0;i<graph.lineSet.size();i++){
                if(graph.lineSet.get(i).contains(shortestDisAddress)) {
                    if(graph.lineSet.get(i).contains(address2)){
                        int index1 = graph.lineSet.get(i).indexOf(shortestDisAddress);
                        int index2 = graph.lineSet.get(i).indexOf(address2);
                        if(index2>index1){
                            if(i==3){
                                if((index2-index1)>(index1+graph.lineSet.get(i).size()-index2)){
                                    for(int k=index2;k<graph.lineSet.get(i).size();k++){
                                        pathStation.add(graph.lineSet.get(i).get(k));
                                        time += graph.lineSetTime.get(i).get(k);
                                    }
                                    for(int j=1;j<index1;j++){
                                        pathStation.add(graph.lineSet.get(i).get(j));
                                        time += graph.lineSetTime.get(i).get(j-1);
                                    }
                                    time += graph.lineSetTime.get(i).get(index1-1);
                                }
                                else{
                                    for(int j=index2;j>=index1+1;j--){
                                        pathStation.add(graph.lineSet.get(i).get(j));
                                        time += graph.lineSetTime.get(i).get(j-1);
                                    }
                                }
                            }
                            else{
                                for(int j=index2;j>=index1+1;j--){
                                    pathStation.add(graph.lineSet.get(i).get(j));
                                    time += graph.lineSetTime.get(i).get(j-1);
                                }
                            }
                        }
                        else{
                            if(i==3){
                                if((index1-index2)>(index2+graph.lineSet.get(i).size()-index1)){
                                    for(int j=index2;j>=1;j--){
                                        pathStation.add(graph.lineSet.get(i).get(j));
                                        time += graph.lineSetTime.get(i).get(j-1);
                                    }
                                    pathStation.add(graph.lineSet.get(i).get(0));
                                    for(int k=graph.lineSet.get(i).size()-1;k>index1;k--){
                                        pathStation.add(graph.lineSet.get(i).get(k));
                                        time += graph.lineSetTime.get(i).get(k);
                                    }
                                    time += graph.lineSetTime.get(i).get(index1);
                                }
                                else{
                                    for(int j=index2;j<index1;j++) {
                                        pathStation.add(graph.lineSet.get(i).get(j));
                                        time += graph.lineSetTime.get(i).get(j);
                                    }
                                }
                            }
                            else{
                                for(int j=index2;j<index1;j++) {
                                    pathStation.add(graph.lineSet.get(i).get(j));
                                    time += graph.lineSetTime.get(i).get(j);
                                }
                            }
                        }
                        time += 1003;//换乘代价
                        time += address1.getDistance().get(shortestDisAddress);
                        setShortestPath(shortestDisAddress);
                        return;
                    }
                }
            }
        }
        //如果找到目标站点为终点站，则结束操作
        if(shortestDisAddress.getAddress().equals(address2.getAddress())){
            setShortestPath(shortestDisAddress);
            time = address1.getDistance().get(address2);
            //System.out.println("耗时："+time+"分钟");
            address1.getDistance().clear();
            return;
        }
        //进行松弛操作
        List<Address> linkedAddress2 = getAllLinkedAddress(shortestDisAddress);
        Address nextAddress;
        for(int i=0;i<linkedAddress2.size();i++) {
            nextAddress = linkedAddress2.get(i);
            if (hasVisited.contains(nextAddress)) {
                continue;
            }
            int shortestPath = address1.getDistance().get(shortestDisAddress) + listTime.get(i);
            if (address1.getDistance().get(nextAddress) != null) {
                //已经计算过到nextAddress的经过距离，那么比较出最小的距离
                if (address1.getDistance().get(nextAddress) > shortestPath) {
                    //重置S1到周围各站的最小路径
                    nextAddress.preAddress = shortestDisAddress;
                    address1.getDistance().remove(nextAddress);
                    address1.getDistance().put(nextAddress,shortestPath);
                }
            }
            else {
                //没有计算过到nextAddress的经过距离
                address1.getDistance().remove(nextAddress);
                nextAddress.preAddress = shortestDisAddress;
                String shortAddress = shortestDisAddress.getAddress();
                String shortAddressPre = shortestDisAddress.preAddress.getAddress();
                int tmp;
                if(shortAddress.equals("大木桥路")&&shortAddressPre.equals("龙华中路")){
                    if(address1.getDistance().get(linkedAddress2.get(0))!=null&&address1.getDistance().get(linkedAddress2.get(1))==null){
                        if(choice==2){
                            tmp = address1.getDistance().get(linkedAddress2.get(0))+listTime.get(0)+listTime.get(1)-1003;
                        }
                        else{
                            tmp = address1.getDistance().get(linkedAddress2.get(0))+listTime.get(0)+listTime.get(1)-3;
                        }
                        if(shortestPath>tmp){
                            shortestPath = tmp;
                            shortestDisAddress.preAddress = graph.lineSet.get(6).get(11);
                        }
                    }
                }
                else if(shortAddress.equals("龙华中路")&&shortAddressPre.equals("大木桥路")){
                    if(address1.getDistance().get(linkedAddress2.get(1))!=null&&address1.getDistance().get(linkedAddress2.get(0))==null){
                        if(choice==2){
                            tmp = address1.getDistance().get(linkedAddress2.get(1))+listTime.get(0)+listTime.get(1)-1003;
                        }
                        else{
                            tmp = address1.getDistance().get(linkedAddress2.get(1))+listTime.get(0)+listTime.get(1)-3;
                        }
                        if(shortestPath>tmp){
                            shortestPath = tmp;
                            shortestDisAddress.preAddress = graph.lineSet.get(3).get(3);
                        }
                    }
                }
                else if(shortAddress.equals("徐家汇")&&shortAddressPre.equals("衡山路")) {
                    if (address1.getDistance().get(linkedAddress2.get(2)) != null&&address1.getDistance().get(linkedAddress2.get(3))==null) {
                        if (choice == 2) {
                            tmp = address1.getDistance().get(linkedAddress2.get(2)) + listTime.get(2) + listTime.get(3) - 1003;
                        } else {
                            tmp = address1.getDistance().get(linkedAddress2.get(2)) + listTime.get(2) + listTime.get(3) - 3;
                        }
                        if (shortestPath > tmp) {
                            shortestPath = tmp;
                            shortestDisAddress.preAddress = graph.lineSet.get(6).get(12);
                        }
                    }
                }
                else if(shortAddress.equals("常熟路")&&shortAddressPre.equals("衡山路")){
                    if (address1.getDistance().get(linkedAddress2.get(2)) != null&&address1.getDistance().get(linkedAddress2.get(3))==null) {
                        if (choice == 2) {
                            tmp = address1.getDistance().get(linkedAddress2.get(2)) + listTime.get(2) + listTime.get(3) - 1003;
                        } else {
                            tmp = address1.getDistance().get(linkedAddress2.get(2)) + listTime.get(2) + listTime.get(3) - 3;
                        }
                        if (shortestPath > tmp) {
                            shortestPath = tmp;
                            shortestDisAddress.preAddress = graph.lineSet.get(8).get(17);
                        }
                    }
                }

                address1.getDistance().put(nextAddress,shortestPath);
            }
        }
        hasVisited.add(shortestDisAddress);
        selectWay(address1,address2,choice);
    }

    //构建最小堆
    private Address extactMin(Address address){
        int minPatn = Integer.MAX_VALUE;
        Address shortestAddress = null;
        //初始化distance列表
        if(address.getDistance().isEmpty()){
            List<Address> linkedAddress = getAllLinkedAddress(address);
            for(int i=0;i<linkedAddress.size();i++){
                address.getDistance().put(linkedAddress.get(i),listTime.get(i));
                linkedAddress.get(i).preAddress = address;
            }
        }
        //类似deleteMin操作
        for(Address s :address.getDistance().keySet()){
            if(hasVisited.contains(s)){
                continue;
            }
            int distance  = address.getDistance().get(s);
            if(distance < minPatn){
                minPatn = distance;
                shortestAddress = s;
            }
        }
        return shortestAddress;
    }

    //取起始站相距最近的站
    private Address getShortestPath(Address address){
        int minPatn = Integer.MAX_VALUE;
        Address shortestAddress = null;
        for(Address s :address.getDistance().keySet()){
            if(hasVisited.contains(s)){
                continue;
            }
            int distance  = address.getDistance().get(s);
            if(distance < minPatn){
                minPatn = distance;
                shortestAddress = s;
            }
        }
        return shortestAddress;
    }

    //获取当前站点相邻的所有站，也包括交叉线上的站
    private List<Address> getAllLinkedAddress(Address address){
        listTime.clear();
        List<Address> linkedAddress = new ArrayList<Address>();
        List<Address> line;
        for(int i=0;i<graph.lineSet.size();i++){
            line = graph.lineSet.get(i);
            if(line.contains(address)){//如果某一条线包含了此站，注意由于重写了hashcode方法，只有name相同，即认为是同一个对象
                int index = line.indexOf(address);
                if(index>0&&index<line.size()-1){
                    //确定当前站点之前的乘坐路径，主要针对于交叉站点
                    line.get(index-1).line = i;
                    line.get(index+1).line = i;
                    linkedAddress.add(line.get(index-1));
                    linkedAddress.add(line.get(index+1));
                    if(i==address.line||address.getAddress().equals(startStation.getAddress())){
                        listTime.add(graph.lineSetTime.get(i).get(index-1));
                        listTime.add(graph.lineSetTime.get(i).get(index));
                    }
                    else{    //需要换乘，需要加上换乘时间，3分钟
                        listTime.add(graph.lineSetTime.get(i).get(index-1)+transferTime);
                        listTime.add(graph.lineSetTime.get(i).get(index)+transferTime);
                    }
                }
                else if(index == 0){
                    if(i == 3){  //四号线
                        line.get(line.size()-1).line = i;
                        line.get(index+1).line = i;
                        linkedAddress.add(line.get(line.size()-1));
                        linkedAddress.add(line.get(index+1));
                        if(i==address.line||address.getAddress().equals(startStation.getAddress())) {
                            listTime.add(graph.lineSetTime.get(i).get(line.size() - 1));
                            listTime.add(graph.lineSetTime.get(i).get(index));
                        }
                        else{   //需要换乘，需要加上换乘时间，3分钟
                            listTime.add(graph.lineSetTime.get(i).get(line.size() - 1)+transferTime);
                            listTime.add(graph.lineSetTime.get(i).get(index)+transferTime);
                        }
                    }
                    else {
                        line.get(index + 1).line = i;
                        linkedAddress.add(line.get(index+1));
                        if(i==address.line||address.getAddress().equals(startStation.getAddress())) {
                            listTime.add(graph.lineSetTime.get(i).get(index));
                        }
                        else{    //需要换乘，需要加上换乘时间，3分钟
                            listTime.add(graph.lineSetTime.get(i).get(index)+transferTime);
                        }
                    }
                }
                else{
                    if(i == 3){  //四号线
                        line.get(index-1).line = i;
                        line.get(index).line = i;
                        linkedAddress.add(line.get(index-1));
                        linkedAddress.add(line.get(index));
                        listTime.add(graph.lineSetTime.get(i).get(index-1));
                        listTime.add(graph.lineSetTime.get(i).get(index));
                        if(i==address.line||address.getAddress().equals(startStation.getAddress())) {
                            listTime.add(graph.lineSetTime.get(i).get(index-1));
                            listTime.add(graph.lineSetTime.get(i).get(index));
                        }
                        else{    //需要换乘，需要加上换乘时间，3分钟
                            listTime.add(graph.lineSetTime.get(i).get(index-1)+transferTime);
                            listTime.add(graph.lineSetTime.get(i).get(index)+transferTime);
                        }
                    }
                    else{
                        line.get(index-1).line = i;
                        linkedAddress.add(line.get(index-1));
                        if(i==address.line||address.equals(startStation)) {
                            listTime.add(graph.lineSetTime.get(i).get(index-1));
                        }
                        else{    //需要换乘，需要加上换乘时间，3分钟
                            listTime.add(graph.lineSetTime.get(i).get(index-1)+transferTime);
                        }
                    }
                }
            }
        }
        return linkedAddress;
    }
    //设置最短路径
    private void setShortestPath(Address address){
        Address pre = address;
        pathStation.add(address);
        while (pre.preAddress != null) {
            pathStation.add(pre.preAddress);
            pre = pre.preAddress;
        }
    }

    private void setShortestPath1(Address address){
        Address pre = address;
        pathFinal.add(address);
        while (pre.preAddress != null) {
            pathFinal.add(pre.preAddress);
            pre = pre.preAddress;
        }
    }

    //分支路径处理
    private void setPathStart(){
        if(pathStationTmpStart.size() != 0) {
            int size = pathStationTmpStart.size();
            for (int i = 0; i < size; i++) {
                addresses.add(pathStationTmpStart.get(size-1-i));
            }
        }
    }
    private void setPathEnd(){
        if(pathStationTmpEnd.size()!=0){
            for(int i=0;i<pathStationTmpEnd.size();i++){
                addresses.add(pathStationTmpEnd.get(i));
            }
        }
    }
    private boolean noTransfer(Address address1, Address address2){
        for(int i=0;i<graph.lineSet.size();i++){
            if(graph.lineSet.get(i).contains(address1)) {
                if(graph.lineSet.get(i).contains(address2)){
                    int index1 = graph.lineSet.get(i).indexOf(address1);
                    int index2 = graph.lineSet.get(i).indexOf(address2);
                    if(index2>index1){
                        if(i==3){
                            if((index2-index1)>(index1+graph.lineSet.get(i).size()-index2)){
                                for(int k=index2;k<graph.lineSet.get(i).size();k++){
                                    pathStation.add(graph.lineSet.get(i).get(k));
                                    time += graph.lineSetTime.get(i).get(k);
                                }
                                pathStation.add(graph.lineSet.get(i).get(0));
                                for(int j=1;j<=index1;j++){
                                    pathStation.add(graph.lineSet.get(i).get(j));
                                    time += graph.lineSetTime.get(i).get(j-1);
                                }
                            }
                            else{
                                for(int j=index2;j>=index1+1;j--){
                                    pathStation.add(graph.lineSet.get(i).get(j));
                                    time += graph.lineSetTime.get(i).get(j-1);
                                }
                                pathStation.add(graph.lineSet.get(i).get(index1));
                            }
                        }
                        else{
                            for(int j=index2;j>=index1+1;j--){
                                pathStation.add(graph.lineSet.get(i).get(j));
                                time += graph.lineSetTime.get(i).get(j-1);
                            }
                            pathStation.add(graph.lineSet.get(i).get(index1));
                        }
                    }
                    else{
                        if(i==3){
                            if((index1-index2)>(index2+graph.lineSet.get(i).size()-index1)){
                                for(int j=index2;j>=1;j--){
                                    pathStation.add(graph.lineSet.get(i).get(j));
                                    time += graph.lineSetTime.get(i).get(j-1);
                                }
                                pathStation.add(graph.lineSet.get(i).get(0));
                                for(int k=graph.lineSet.get(i).size()-1;k>=index1;k--){
                                    pathStation.add(graph.lineSet.get(i).get(k));
                                    time += graph.lineSetTime.get(i).get(k);
                                }
                            }
                            else{
                                for(int j=index2;j<index1;j++) {
                                    pathStation.add(graph.lineSet.get(i).get(j));
                                    time += graph.lineSetTime.get(i).get(j);
                                }
                                pathStation.add(graph.lineSet.get(i).get(index1));
                            }
                        }
                        else{
                            for(int j=index2;j<index1;j++) {
                                pathStation.add(graph.lineSet.get(i).get(j));
                                time += graph.lineSetTime.get(i).get(j);
                            }
                            pathStation.add(graph.lineSet.get(i).get(index1));
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

}
