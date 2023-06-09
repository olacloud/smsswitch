package cobbe.smsswitch.util ;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Set ;
import java.util.HashSet ;
import lombok.Getter;
import lombok.Setter;

@Slf4j
public class KeyList implements Serializable  {

@Getter
private String id ;

@Getter @Setter
private Set values ;

public KeyList(String id) {
this.id = id ;
values = new HashSet() ;
}

public void addValue(Object value) {
log.info("adding value" + value ) ;
values.add(value) ;
}

@Override
public String toString() {
return id + "|" + values ;
}

}