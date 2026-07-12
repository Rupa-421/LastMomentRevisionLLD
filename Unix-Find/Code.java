interface FileNode{

}
class File implements FileNode{
    private String name;
    private String extension;
    private double size;
    private String path;
    public File(String name,String extension,double size,String path){
        this.name = name;
        this.extension = extension;
        this.size = size;
        this.path = path;
    }
    public String getExtension(){
        return extension;
    }   
    public double getSize(){
        return size;
    }
    public String getPath(){
        return path;
    }
}
class Directory implements FileNode{
    private String name;
    private List<FileNode> children;
    public Directory(String name){
        this.name = name;
        this.children = new ArrayList<>();
    }
    public List<FileNode> getChildren(){
        return children;
    }
    public void add(FileNode node){
        children.add(node);
    }
}

interface Filter{
    boolean matches(File file);
}

class SizeFilter implements Filter{
    private double minSize;
    public SizeFilter(double minSize){
        this.minSize = minSize
    }
    @Override 
    public boolean matches(File file){
        return file.getSize() > minSize;
    }
}

class ExtensionFilter implements Filter{
    private String extension;
    public ExtensionFilter(String extension){
        this.extension = extension;
    }

    @Override
    public boolean matches(File file){
        return file.getExtension().equals(extension);
    }
}

class FindService{
    public List<File> find(FileNode root,Filter filter){
        Set<String> visited = new HashSet<>();
        List<File> result = new ArrayList<>();
        dfs(root,filter,visited,result);
        result.sort(Comparator.comparing(File::getPath));
        return result;
    }
    private void dfs(FileNode node, Filter filter, Set<String> visited,List<File> result){
        if(node instanceof File file){
            if(!visited.contains(file.getPath()) && filter.matches(file)){
                visited.add(file.getPath());
                result.add(file);
            }
            return ;
        }
        Directory dir = (Directory) node;
        for(FileNode child: dir.getChildren()){
            dfs(child,filter,visited,result);
        }
    }
}