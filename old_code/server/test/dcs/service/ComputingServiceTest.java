package dcs.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ComputingServiceTest {
    @Mock private List<Integer> mockedList;
    
    @Test public void passingTest() {
        assertTrue(true);
    }
    
    @Test public void simpleMockTest() {
        mockedList.add(0);
        
        verify(mockedList).add(0);
    }
}
