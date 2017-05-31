package hello.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    private Path rootLocation;

    private String shopName;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
        this.shopName = "shop3/";
    }

    @Override
    public void store(MultipartFile file,String name) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }
            if(Files.exists(rootLocation.resolve(file.getOriginalFilename()))){
                deleteOne(file.getOriginalFilename());
            }
            //Files.copy(file.getInputStream(), rootLocation.resolve(file.getOriginalFilename()));
            Files.copy(file.getInputStream(), rootLocation.resolve(name));
        } catch (FileAlreadyExistsException e) {
            throw new StorageException("File is exist" + file.getOriginalFilename(), e);
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(path -> this.rootLocation.relativize(path));
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void deleteOne(String filename) {
        System.out.println(filename);
        Path path = rootLocation.resolve(filename);
        System.out.println(path);
        try {
            System.out.println("............."+Files.deleteIfExists(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void init() {
        rootLocation = Paths.get("src\\main\\resources\\static\\img/"+shopName);
        try {
            if(Files.isDirectory(rootLocation)) {
                return;
            }
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
