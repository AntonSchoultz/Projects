package za.co.discoverylife.st2git;

import java.util.Comparator;

public class ReferenceByRepoKey implements Comparator<Reference>
{

  public int compare(Reference ref1, Reference ref2)
  {
    // first by repo
    int n = ref1.repo.compareTo(ref2.repo);
    if(n!=0) return n;
    // then by date
    n = ref1.timestamp.compareTo(ref2.timestamp);
    return n;
  }
}
