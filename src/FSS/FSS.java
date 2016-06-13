package FSS;

import util.Util;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by moura on 6/12/16.
 */
public class FSS {

    public static final int quantidadePesos = 12;

    public void centralExecution(){

        double iterations = 0;
        Fish[] school = new Fish[Util.FISH_QUANTITY];

        initializeSchool(school);

        while(iterations <= Util.NUMBER_OF_ITERATIONS){

            double individualStep = Util.STEP_IND_INCIAL - (Util.STEP_IND_INCIAL - Util.STEP_IND_FINAL)*(iterations/Util.NUMBER_OF_ITERATIONS);
            individualOperator(school, individualStep);

            feeding_operator(school);

            double[] school_instinctive = colletive_instinctive_operator(school);

            double step_volitive = Util.STEP_COLECTIVE_INCIAL - (Util.STEP_COLECTIVE_INCIAL-Util.STEP_COLECTIVE_FINAL)*(iterations/Util.NUMBER_OF_ITERATIONS);
            individualOperator(school,individualStep);

            colletive_volitive_operator(school,step_volitive,school_instinctive);

            Arrays.sort(school,new FSS_ComparatorByBestFitness());
            for(Fish fish: school) {
                System.out.println("nei="+fish.neighbor.fitness+" now="+fish.current.fitness+" best="+fish.best.fitness+" weight="+fish.current_weight);
            }

            iterations = iterations + 1;
        }

    }

    private static int colletive_volitive_operator(Fish[] school,double step_size,double[] school_instinctive){

        double[] school_barycentre=new double[quantidadePesos];
        double[] sum_prod=new double[quantidadePesos];
        double sum_weight_now = 0;
        double sum_weight_past = 0;

        //clear
        for(int i=0;i<sum_prod.length;i++){
            sum_prod[i]=0;
            school_barycentre[i]=0;
        }

        //for each fish contribute with your neighbor position and weight
        for(Fish fish: school){
            for(int i=0;i<fish.delta_x.length;i++){
                sum_prod[i]+= fish.neighbor.variables[i]*fish.current_weight;
            }
            //sum weight
            sum_weight_now+=fish.current_weight;
            sum_weight_past+=fish.past_weight;
        }
        //calculate barycentre
        for(int i=0;i<sum_prod.length;i++){
            school_barycentre[i]=sum_prod[i]/sum_weight_now;
        }

        double direction=0;
        if(sum_weight_now>sum_weight_past){
            //weight gain -> contract
            direction=1;
        }
        else {
            //weight loss -> dilate
            direction=-1;
        }

        int count_success=0;
        for(Fish fish: school) {

            double de = euclidian_distance(fish.neighbor.variables,school_barycentre);

            //take care about zero division
            if(de!=0){
                //
                for(int i=0;i<fish.neighbor.variables.length;i++){

                    //continue to update neighbor with dilate/shrink
                    fish.neighbor.variables[i] += ( step_size * direction * ThreadLocalRandom.current().nextDouble(0,1.1) * (fish.neighbor.variables[i]-school_barycentre[i]) ) / de;

                    //TODO take care about bounds of search space
//                    if(fish.neighbor.variables[i]<f.getMin()) fish.neighbor.variables[i]=f.getMin();
//                    if(fish.neighbor.variables[i]>f.getMax()) fish.neighbor.variables[i]=f.getMax();
                }
                //evaluate new current solution
//                fish.neighbor.fitness = MeanSquareError(fish.neighbor.variables);

                //update current if neighbor is best
                fish.volitive_move_success = false;
                if(fish.neighbor.fitness<fish.current.fitness){
                    FSS_Solution.copy(fish.neighbor, fish.current);
                    fish.volitive_move_success = true;
                    count_success++;
                }

                //if need replace best solution
                if(fish.current.fitness<fish.best.fitness){
                    FSS_Solution.copy(fish.current, fish.best);
                }
            } else {
                //warning user
                System.err.println("bypass volitive operator (zero division)");
            }
        }

        return count_success;
    }

    private static double euclidian_distance(double[] a,double[] b){
        double sum=0;
        for(int i=0;i<a.length;i++){
            sum+=Math.pow(a[i]-b[i],2.0);
        }
        return Math.sqrt(sum);
    }

    private static double[] colletive_instinctive_operator(Fish[] school){

        //
        double[] school_instinctive = calculate_instinctive_direction(school);

        //
        for(Fish fish: school) {
            //use current as template to neighbor
            FSS_Solution.copy(fish.current, fish.neighbor);

            //update neighbor with instinctive direction
            for(int i=0;i<fish.neighbor.variables.length;i++){
                fish.neighbor.variables[i] += school_instinctive[i];
            }
        }

        return school_instinctive;
    }

    private static double[] calculate_instinctive_direction(Fish[] school) {
        //
        double[] school_instinctive = new double[quantidadePesos];
        double[] sum_prod=new double[quantidadePesos];
        double sum_fitness_gain = 0;

        //clear
        for(int i=0;i<sum_prod.length;i++){
            sum_prod[i]=0;
            school_instinctive[i]=0;
        }

        //for each fish contribute with your direction scaled by your fitness gain
        for(Fish fish: school){
            //only good fishes
            if(fish.individual_move_success){
                //sum product of solution by fitness gain
                for(int i=0;i<fish.delta_x.length;i++){
                    sum_prod[i]+= fish.delta_x[i]*fish.fitness_gain_normalized;
                }
                //sum fitness gains
                sum_fitness_gain+=fish.fitness_gain_normalized;
            }
        }

        //calculate global direction of good fishes
        for(int i=0;i<sum_prod.length;i++){
            //take care about zero division
            if(sum_fitness_gain!=0){
                school_instinctive[i]=sum_prod[i]/sum_fitness_gain;
            }
            else {
                school_instinctive[i]=0;
            }
        }
        return school_instinctive;
    }

    private static void feeding_operator(Fish[] school){

        //sort school by fitness gain
        Arrays.sort(school,new FSS_ComparatorByFitnessGain());

        //max absolute value of fitness gain
        double abs_delta_f_max=Math.abs(school[0].delta_f);
        double abs_delta_f_max2=Math.abs(school[school.length-1].delta_f);
        if(abs_delta_f_max2>abs_delta_f_max) abs_delta_f_max=abs_delta_f_max2;

        //take care about zero division
        if(abs_delta_f_max!=0){
            //calculate normalized gain
            for(Fish fish: school){
                fish.fitness_gain_normalized = fish.delta_f/abs_delta_f_max;
            }

            //feed all fishes
            for(Fish fish: school) {
                //
                fish.past_weight = fish.current_weight;
                fish.current_weight += fish.fitness_gain_normalized;
                //take care about min and max weight
                if(fish.current_weight<Util.W_MINIMUM) fish.current_weight=Util.W_MINIMUM;
                if(fish.current_weight>Util.W_SCALE) fish.current_weight=Util.W_SCALE;
            }
        }
        else {
            //warning user
            System.err.println("bypass feeding (zero division)");
        }

    }

    public void individualOperator(Fish[] school, double step){

        for(Fish fish: school){
            FSS_Solution.copy(fish.current, fish.neighbor);

            //calculate displacement
            for(int i=0;i<quantidadePesos;i++){
                fish.delta_x[i] = ThreadLocalRandom.current().nextDouble(-1,1.1)*step;
                fish.neighbor.variables[i] = fish.delta_x[i];
                //TODO take care about bounds of search space
            }

//            fish.neighbor.fitness = MeanSquaredError(fish.neighbor.variables); TODO funcao de fitness

            //calculate fitness difference
            fish.delta_f = fish.neighbor.fitness-fish.current.fitness;

            //update current if neighbor is best
            fish.individual_move_success = false;
            if(fish.neighbor.fitness<fish.current.fitness){
                FSS_Solution.copy(fish.neighbor, fish.current);
                fish.individual_move_success = true;
            }

            //if need replace best solution
            if(fish.current.fitness<fish.best.fitness){
                FSS_Solution.copy(fish.current, fish.best);
            }
        }
    }

    public void initializeSchool(Fish[] school){

        for(Fish fish : school){
            fish = new Fish();

            fish.current = new FSS_Solution(quantidadePesos);
            fish.best = new FSS_Solution(quantidadePesos);
            fish.neighbor = new FSS_Solution(quantidadePesos);

//            fish.current.fitness = MeanSquaredError(fish.current.variables); TODO fazer a funcao mse que recebe um array de pesos e retorna um fitness da solucao

            FSS_Solution.copy(fish.current, fish.best);
            FSS_Solution.copy(fish.current, fish.neighbor);

            fish.delta_x = new double[quantidadePesos];

//            fish.current_weight = ThreadLocalRandom.current().nextDouble(1,Util.W_SCALE + 1); TODO nao tenho certeza de como inicializar os pesos dos peixes...
            fish.current_weight = Util.W_SCALE/2;
            fish.past_weight = fish.current_weight;
        }

    }


}