package m;

import base.Simulator;
import base.Vehicle;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.CsvToBeanFilter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.reflections.Reflections;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, InterruptedException {
        String timeStamp = new SimpleDateFormat("MM.dd.HH.mm.ss").format(new Date());
        String masterFolder = "C:/Users/tobyf/IdeaProjects/Junction_Model/Data/";
        String resultsFolderName = new SimpleDateFormat("MM.dd").format(new Date()) + "_Results/";
        Reflections reflections = new Reflections("");
        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Simulator> simulations = new LinkedList<Simulator>();

        //read vehicle details
        Set<Class<? extends Vehicle>> vehicleClasses = reflections.getSubTypesOf(Vehicle.class);
        for (Class vehicleClass : vehicleClasses) {
            new CsvToBeanBuilder(new FileReader(masterFolder + "vehicles.csv"))
                    .withType(vehicleClass).withFilter(new NameFilter(vehicleClass.getName())).build().parse();
        }

        //read simulation details
        Set<Class<? extends Simulator>> simulatorClasses = reflections.getSubTypesOf(Simulator.class);
        for (Class simulatorClass : simulatorClasses) {
            simulations.addAll(new CsvToBeanBuilder(new FileReader(masterFolder + "simulations.csv"))
                    .withType(simulatorClass).withFilter(new NameFilter(simulatorClass.getName())).build().parse());
        }

        //execute each simulation
        for (final Simulator simulation : simulations) {
            es.execute(simulation);
        }
        //blocks until all threads terminate
        es.shutdown();
        es.awaitTermination(100, TimeUnit.MINUTES);

        //output results and vehicles to CSV with timestamp prefix

        File file = new File(masterFolder + resultsFolderName + timeStamp + "_results.csv");
        file.getParentFile().mkdirs();
        Writer writer = new FileWriter(file);
        StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
        beanToCsv.write(simulations);
        writer.close();
        Desktop.getDesktop().open(file);
        //copies vehicle paramaters file to timestamped version
        Files.copy(Paths.get(masterFolder + "vehicles.csv"), Paths.get(masterFolder + resultsFolderName + timeStamp + "_vehicles.csv"));
        System.out.println("\n******************************\nSimulation Results Reference: " + timeStamp + "\n******************************");
    }
}

class NameFilter implements CsvToBeanFilter {

    String name;

    public NameFilter(String name) {
        this.name = name;
    }

    @Override
    public boolean allowLine(String[] line) {
        String value = line[0];
        boolean result = name.equals(value);
        return result;
    }
}

