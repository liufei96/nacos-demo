package ${classPath};

public class ${className} {
    
   private Integer ${Id};
   private String ${userName};
   private String ${password};
   
    
    
    public Integer get${Id?cap_first}(){
    
        return ${Id};
    }
    
    public void set${Id?cap_first}(Integer ${Id}){
    
        this.${Id}=${Id};
    }
    
    public String get${userName?cap_first}(){
        return ${userName};
    }

    public void set${userName?cap_first}(String ${userName}){
        this.${userName}=${userName};
        
    }
    
    public String get${password?cap_first}(){
        return ${password};
    }
    
    public void set${password?cap_first}(String ${password}){
        this.${password}=${password};
    }

}