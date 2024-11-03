# How to Release to Maven Central

1. Cleanup

    ```shell
    mvn release:clean
    ```
   delete git tag, if needed:
    ```shell
    git tag -d v0.1.0
    ```

2. Prepare the release:
    ```shell
    mvn release:prepare \
      -Dresume=false \
      -DpushChanges=false \
      -DprojectVersionPolicyId=SemVerVersionPolicy
    ```

3. Perform the release

    ```shell
    GPG_TTY=$(tty) && \
    export GPG_TTY && \
    mvn release:perform -DlocalCheckout=true
    ```
   https://stackoverflow.com/a/57591830/3315474

4. Push

    ```shell
    git push origin 
    git push origin --tags
    ```
