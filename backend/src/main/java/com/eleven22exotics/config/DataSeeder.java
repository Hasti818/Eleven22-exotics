package com.eleven22exotics.config;

import com.eleven22exotics.entity.*;
import com.eleven22exotics.repository.CarRepository;
import com.eleven22exotics.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/** Seeds the 12 demo cars and the admin account on first start. All prices are fictional demo data. */
@Component
public class DataSeeder implements CommandLineRunner {
    private static final Map<String, String> DESCRIPTIONS = Map.of(
        "Performance", "A sharp, brutally quick performance machine that is as comfortable on a daily drive as it is on an open road.",
        "Grand Tourer", "Long-distance luxury with serious power. Effortless pace, a hand-built feel and room to enjoy it.",
        "Sports Car", "Precision engineering and razor-sharp handling. A driver's car in the purest sense.",
        "Supercar", "Extreme performance wrapped in a head-turning design. Built to be heard, seen and remembered.",
        "Luxury SUV", "Commanding presence, supercar-level power and space for the whole crew.");

    // slug, brand, model, year, category, hp, transmission, daily, monthly, engine, drivetrain, fuel, seats, 0-100, top speed, doors
    // Figures are approximate: verify against manufacturer data before publishing.
    private static final Object[][] ROWS = {
        {"bmw-m4-competition","BMW","M4 Competition",2024,"Performance",510,"Automatic",2499,49999,"3.0L twin-turbo inline-6","AWD","Petrol",4,"3.5",250,2},
        {"bmw-m8-competition","BMW","M8 Competition",2024,"Grand Tourer",625,"Automatic",3499,69999,"4.4L twin-turbo V8","AWD","Petrol",4,"3.2",250,2},
        {"porsche-911-carrera-s","Porsche","911 Carrera S",2024,"Sports Car",443,"Automatic",3299,64999,"3.0L twin-turbo flat-6","RWD","Petrol",4,"3.5",308,2},
        {"porsche-911-turbo-s","Porsche","911 Turbo S",2024,"Supercar",650,"Automatic",5499,104999,"3.7L twin-turbo flat-6","AWD","Petrol",4,"2.7",322,2},
        {"porsche-718-cayman-gt4-rs","Porsche","718 Cayman GT4 RS",2023,"Sports Car",500,"Automatic",5999,114999,"4.0L naturally aspirated flat-6","RWD","Petrol",2,"3.4",315,2},
        {"mercedes-amg-gt-63","Mercedes-AMG","GT 63",2024,"Grand Tourer",639,"Automatic",3799,74999,"4.0L twin-turbo V8","AWD","Petrol",4,"3.2",315,2},
        {"mercedes-amg-g-63","Mercedes-AMG","G 63",2024,"Luxury SUV",577,"Automatic",3999,79999,"4.0L twin-turbo V8","AWD","Petrol",5,"4.5",220,5},
        {"audi-rs7-performance","Audi","RS7 Performance",2024,"Performance",621,"Automatic",2999,59999,"4.0L twin-turbo V8","AWD","Petrol",5,"3.4",250,5},
        {"audi-r8-v10-performance","Audi","R8 V10 Performance",2023,"Supercar",602,"Automatic",4499,89999,"5.2L naturally aspirated V10","AWD","Petrol",2,"3.1",331,2},
        {"lamborghini-huracan-evo","Lamborghini","Huracán EVO",2023,"Supercar",631,"Automatic",7499,144999,"5.2L naturally aspirated V10","AWD","Petrol",2,"2.9",325,2},
        {"lamborghini-urus","Lamborghini","Urus",2024,"Luxury SUV",641,"Automatic",5999,114999,"4.0L twin-turbo V8","AWD","Petrol",5,"3.6",305,5},
        {"ferrari-296-gtb","Ferrari","296 GTB",2024,"Supercar",819,"Automatic",8999,169999,"3.0L twin-turbo V6 plug-in hybrid","RWD","Hybrid",2,"2.9",330,2},
    };

    private final CarRepository cars;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataSeeder(CarRepository cars, UserRepository users, PasswordEncoder encoder,
                      @Value("${app.admin.email}") String adminEmail, @Value("${app.admin.password}") String adminPassword) {
        this.cars = cars;
        this.users = users;
        this.encoder = encoder;
        this.adminEmail = adminEmail.toLowerCase();
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!users.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("Eleven22");
            admin.setEmail(adminEmail);
            admin.setPasswordHash(encoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            users.save(admin);
        }
        if (cars.count() > 0) return;
        for (Object[] r : ROWS) {
            Car c = new Car();
            c.setBrand((String) r[1]);
            c.setModel((String) r[2]);
            c.setModelYear((Integer) r[3]);
            c.setCategory((String) r[4]);
            c.setDescription(DESCRIPTIONS.get(r[4]));
            c.setHorsepower((Integer) r[5]);
            c.setTransmission((String) r[6]);
            c.setDailyPrice(BigDecimal.valueOf((Integer) r[7]));
            c.setMonthlyPrice(BigDecimal.valueOf((Integer) r[8]));
            c.setEngine((String) r[9]);
            c.setDrivetrain((String) r[10]);
            c.setFuelType((String) r[11]);
            c.setSeats((Integer) r[12]);
            c.setZeroToOneHundred(new BigDecimal((String) r[13]));
            c.setTopSpeed((Integer) r[14]);
            c.setDoors((Integer) r[15]);
            c.setStatus(CarStatus.ACTIVE);
            String url = "assets/images/" + r[0] + ".webp";
            c.setMainImageUrl(url);
            CarImage img = new CarImage();
            img.setCar(c);
            img.setImageUrl(url);
            img.setAltText(c.getBrand() + " " + c.getModel());
            img.setImageType(ImageType.MAIN);
            c.getImages().add(img);
            cars.save(c);
        }
    }
}
